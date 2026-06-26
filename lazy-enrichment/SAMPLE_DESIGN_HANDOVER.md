# Lazy Enrichment Sample — Design Handover

## What this document is

A self-contained briefing for an agent picking up the `lazy-enrichment` OpenCQRS sample. Covers what the sample now does, why it does it that way, what was deliberately taken out, and what is still open. Sibling handovers in this folder cover adjacent topics:

- `BLOG_REVISION_HANDOVER.md` — for the parallel revision of the OpenCQRS blog post that originally recommended a less-clean variant of this sample.
- `GATEWAY_PATTERN_HANDOVER.md` — about an Event-Encryption side topic (Jackson-Marshaller decorator vs. `@Encrypted` field annotation); independent of the sample design.

## Repo coordinates

- Path: `/Users/kersten/dev/df/opencqrs-samples/lazy-enrichment`
- Branch: `feat/lazy-enrichment-example`
- OpenCQRS version: `1.0.0` (Spring Boot Starter)
- Build state at handover: `./gradlew compileJava compileTestJava` and `./gradlew test` both green.

## What the sample demonstrates

A loan-application service that illustrates **lazy schema enrichment**: an old event in the store (`LoanApplicationAppliedEvent`) was written before the `manualReviewResult` field existed. The first time anyone tries to approve such a loan, the approve handler notices the missing field, fetches the value from an external `ManualReviewService`, writes an enrichment event, and proceeds with the approval — all in **one** OpenCQRS handler invocation, hence one atomic ESDB append.

## The design path that led here

The sample originally implemented the pattern shown in the OpenCQRS blog post on schema evolution: a `LoanCommandGateway` Spring component wrapping `CommandRouter` and dispatching `EnsureLoanEnrichmentCommand` before forwarding the original business command. A peer review with Frank Scheffler and Nik (slack discussion preserved in `BLOG_REVISION_HANDOVER.md`) surfaced three structural defects that apply to **any** "two sequential `commandRouter.send(…)`" pattern, regardless of whether the wrapper is a service-style gateway or a `SourcingMode.NONE` `@CommandHandling` interceptor:

1. **No transaction over the two `send(…)` calls.** If the second send throws, the first one is already committed.
2. **Breaks `SubjectIsOnEventId` optimistic locking.** The gateway appends an event between client-`send` and business handler, moving the subject's tip from version X to Y. The client's precondition fails.
3. **Idempotency only half-solved.** Enrich is idempotent via a null-check; the business command needs its own state-based guard.

A separate misframing also surfaced in the discussion: the worry that "follow-up sourcing might not see the enrichment event due to eventual consistency". That worry is wrong on the same subject — OpenCQRS guarantees per-subject Strong Consistency on the command side. Eventual consistency in OpenCQRS applies only to projections and to cross-subject reads. The real problems are the three above, and they exist even under perfect SC.

The sample was rewritten to the single-handler enricher (this iteration), which removes all three defects in one move: one handler invocation, one atomic append, one precondition check, standard handler-level idempotency.

A production data point reinforces the design choice: the Axon-based codebase `/Users/kersten/dev/df/syna/esp-middleware` was scanned and uses the same shape — interceptors only decorate/validate, multi-effect commands publish multiple events from a single aggregate handler via `AggregateLifecycle.apply(...)`. The pattern transfers across frameworks because the underlying property (one handler invocation = one atomic event append) is identical.

## Current code shape

### File inventory (post-refactor)

```
src/main/java/com/example/cqrs/
├── LazyEnrichmentApplication.java           # Spring Boot entry, unchanged
├── api/
│   └── LoanApplicationController.java       # uses CommandRouter directly, no gateway
├── domain/LoanApplication/
│   ├── LoanApplicationHandling.java         # @CommandHandlerConfiguration with apply + approve handlers
│   ├── LoanRequest.java                     # sourced state, plain record
│   ├── ManualReviewService.java             # interface for external enrichment source
│   ├── StubManualReviewService.java         # @Service stub returning "COMPLIANT"
│   ├── commands/
│   │   ├── LoanApplicationCommand.java      # sealed interface, permits Apply + Approve
│   │   ├── ApplyLoanRequestCommand.java     # SubjectCondition.PRISTINE
│   │   └── ApproveLoanCommand.java          # SubjectCondition.EXISTS
│   └── events/
│       ├── LoanApplicationAppliedEvent.java
│       ├── LoanApplicationEnrichedEvent.java
│       └── LoanApplicationApprovedEvent.java
└── readmodel/
    ├── LoanApplicationProjection.java       # @EventHandling per event type
    ├── LoanApplicationView.java             # JPA entity
    └── LoanApplicationViewRepository.java   # Spring Data repo

src/test/java/com/example/cqrs/domain/LoanApplication/
└── LoanApplicationHandlingTest.java         # 5 tests, all green
```

### The single-handler enricher (the central piece)

`LoanApplicationHandling.handle(ApproveLoanCommand, …)`:

```java
@CommandHandling
public void handle(
        LoanRequest request,
        ApproveLoanCommand command,
        CommandEventPublisher<LoanRequest> publisher,
        @Autowired ManualReviewService manualReviewService) {

    String reviewResult = request.manualReviewResult();
    if (reviewResult == null) {
        reviewResult = manualReviewService.fetchReviewResult(command.getApplicationId());
        publisher.publish(new LoanApplicationEnrichedEvent(command.getApplicationId(), reviewResult));
    }

    if (!"COMPLIANT".equals(reviewResult)) {
        throw new IllegalStateException("Loan cannot be approved, review result: " + reviewResult);
    }
    publisher.publish(new LoanApplicationApprovedEvent(command.getApplicationId()));
}
```

The `@Autowired` parameter for `ManualReviewService` works because OpenCQRS's `CommandHandlingAnnotationProcessingAutoConfiguration` treats any `@Autowired`-annotated handler parameter as a runtime DI resolution (verified in source). No constructor or field injection needed.

### How the pattern addresses each lazy-enrichment requirement

| Lazy-enrichment requirement | How the code satisfies it |
|---|---|
| Backfill only when the entity is next touched | `if (reviewResult == null)` check on the sourced state — no batch, no migration |
| Backfill is persisted to the stream | `publisher.publish(LoanApplicationEnrichedEvent)` appended permanently |
| Original immutable events untouched | The old `LoanApplicationAppliedEvent` is read-only; a new event is appended |
| Idempotent on retry | The same null-check skips the branch on subsequent invocations |
| Atomic enrich + business | Both `publish(…)` calls live in one handler invocation → one ESDB append |
| OL-compatible (`SubjectIsOnEventId`) | The whole append commits against the sourced version; no interleaved write |
| Data source may live outside the store | `ManualReviewService` interface — stub today, real client tomorrow |

## What was deliberately removed and why

| Removed | Why |
|---|---|
| `LoanCommandGateway.java` | The two-`send(…)` pattern; defects listed above. |
| `commands/EnsureLoanEnrichmentCommand.java` | Existed only to be the second command in the gateway pair; not needed when enrichment is inline. |
| `commands/VersionedCommand.java` | Optimistic-locking primitive. OL is a separate concern from lazy enrichment; properly demonstrated in the sibling `applying-optimistic-locking` sample (PR #17). Keeping it here muddled the lesson. |
| `Versioned.java` | Same reason as above. |
| `test/.../LoanCommandGatewayTest.java` | Tested the gateway that no longer exists. |

The expectation is that a reader who wants OL reads the dedicated sample; mixing both concepts in one sample obscures both.

## What was deliberately kept

- `ApplyLoanRequestCommand` keeps `SubjectCondition.PRISTINE` and its convenience constructor — it represents the "fresh subject" path that the lazy-enrichment story does not touch.
- `LoanApplicationProjection` and `LoanApplicationView` were left untouched. They project the three event types into a read model. They never needed `Versioned`; they only fed it when OL was in play.
- The sealed-interface design for `LoanApplicationCommand` is retained — useful for compile-time exhaustiveness checks on the command type, even with only two permitted subtypes today.

## Test approach

`@CommandHandlingTest` (from `framework-test:1.0.0`) loads a Spring slice that exposes a `CommandHandlingTestFixture<…>` per `@CommandHandling` method. External dependencies are provided as `@MockitoBean` per the framework's documented convention:

```java
@CommandHandlingTest
class LoanApplicationHandlingTest {

    @MockitoBean
    ManualReviewService manualReviewService;

    @Test
    void shouldEnrichAndApproveInOneAppendWhenNotYetEnriched(
            @Autowired CommandHandlingTestFixture<ApproveLoanCommand> fixture) {
        when(manualReviewService.fetchReviewResult("app-id")).thenReturn("COMPLIANT");

        fixture.given(new LoanApplicationAppliedEvent("app-id", "applicant-1", "10000"))
                .when(new ApproveLoanCommand("app-id"))
                .expectSuccessfulExecution()
                .expectEvents(
                        new LoanApplicationEnrichedEvent("app-id", "COMPLIANT"),
                        new LoanApplicationApprovedEvent("app-id")
                );
    }
}
```

Five tests cover: PRISTINE create, duplicate rejection, inline enrich+approve (legacy event only), approve-only (already enriched), and approval rejection on non-COMPLIANT review result. All green.

## What is intentionally out of scope (and where it belongs)

Lazy enrichment in a single handler works because both events target the same subject and the same handler invocation. The README explicitly lists the three cases that outgrow this pattern and points to Saga:

1. **Cross-aggregate enrichment** — enrich on one subject, business on another. No shared atomic append.
2. **Asynchronous / long-running enrichment** — would block a synchronous command handler.
3. **Multi-step enrichment with intermediate state** — needs explicit process state.

All three are textbook Saga territory. The sibling `implementing-sagas` sample is the right place for those stories; this sample resists the temptation to half-bridge them.

## Outstanding work for the next agent

- **Root README** (`/Users/kersten/dev/df/opencqrs-samples/README.md`) currently does not list `lazy-enrichment` at all. Adding a one-line entry would complete the integration.
- **Bruno collection / HTTP examples**: there is no Bruno collection for this sample yet (other samples have one in `clients/`). The `ApproveRequest` shape is `{ "applicationId": "<id>" }` — no `expectedVersion` field — should an `applying-optimistic-locking`-style Bruno set be added for this sample, it must reflect that.
- **Diagrams**: other samples in this repo have SVG sequence diagrams. The README would benefit from one showing "legacy stream → approve sourced → enrich + approve as one append". Generator scripts in other samples may be reused.
- **Optional follow-up**: a second handler showing what an `EditLoanDetailsCommand` (another EXISTS-conditioned command) would look like, with the same `ensureEnriched(...)`-style helper extracted as a private method. This would demonstrate how the pattern scales when multiple handlers need the same backfill.
- **Linkage with the blog revision** (`BLOG_REVISION_HANDOVER.md`): when the blog post is updated to recommend the single-handler enricher, the README here could link out to the post as further reading, and the post could link back to this sample as the canonical implementation. Sequencing matters — finalize the blog before adding the back-link.

## Reference reading for the next agent

- `README.md` in this folder — written for end users, narrates the same design rationale.
- `BLOG_REVISION_HANDOVER.md` in this folder — full context on the blog defects this sample was rewritten to avoid.
- OpenCQRS framework sources in `~/.gradle/caches/modules-2/files-2.1/com.opencqrs/`:
  - `framework/1.0.0/.../framework-1.0.0-sources.jar` → `command/CommandRouter.java`, `command/CommandEventPublisher.java`, `serialization/EventDataMarshaller.java`, `upcaster/EventUpcaster.java`.
  - `framework-spring-boot-autoconfigure/1.0.0/.../*-sources.jar` → `command/CommandHandlingAnnotationProcessingAutoConfiguration.java` (proves `@Autowired` handler params are supported), `command/CommandHandling.java`, `command/CommandHandlerConfiguration.java`, `serialization/JacksonEventDataMarshallerAutoConfiguration.java` (the `@ConditionalOnMissingBean` story).
  - `framework-test/1.0.0/.../*-sources.jar` → `command/CommandHandlingTest.java` (documents `@MockitoBean` convention), `command/CommandHandlingTestFixture.java` (`expectEvents(Object...)`, `expectSingleEvent`, `expectException`).
- `applying-optimistic-locking` PR https://github.com/open-cqrs/opencqrs-samples/pull/17 — the proper home for the `VersionedCommand` pattern that was removed from this sample.
- `implementing-sagas` sample in this repo — the right destination for the cross-aggregate / async / multi-step cases.
- Production data point: `/Users/kersten/dev/df/syna/esp-middleware` — Axon codebase confirming the cross-framework validity of the "multiple events from one handler" pattern. Five interceptors there, none of them dispatch second commands; four aggregates use multi-`apply(...)` in single handlers.

## Don'ts for the next agent

- Don't reintroduce a `LoanCommandGateway`. The discussion that led to its removal is documented; reverting it would silently bring back the three defects.
- Don't add `VersionedCommand` to commands in this sample to "make OL work too". It mixes lessons. If OL needs to be shown alongside lazy enrichment, do it as a third sample or a dedicated section in the blog post — not by reaching into this code.
- Don't switch the enrichment to be triggered by an external command dispatched from the handler (regression to the two-`send(…)` pattern). The inline pattern is the point.
- Don't strip the `ManualReviewService` indirection to inline a hardcoded `"COMPLIANT"` string. The interface is the seam that signals "real systems plug a real client in here" and is part of the lesson.
