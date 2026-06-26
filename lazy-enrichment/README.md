# Schema Evolution: Upcasters and Lazy Enrichment

-----

**NOTE**

This sample assumes you have completed the official [OpenCQRS tutorial](https://docs.opencqrs.com/tutorials/).

Companion reading: [Evolving Event-Sourced Systems](https://docs.opencqrs.com/blog/evolving-event-sourced-systems/) on the OpenCQRS blog.

-----

Events in an event-sourced system are immutable. When a new requirement demands a field that did not exist when older events were written, you cannot rewrite history. There are three strategies for handling this — and they form a decision tree:

```
Can the missing data be derived from existing fields?
├── Yes → Calculate via Upcaster
└── No → Does a meaningful default exist?
         ├── Yes → Compensate via Upcaster
         └── No  → Enrich Lazily
```

This sample demonstrates all three on the same domain — the loan-application service. One event (`LoanApplicationAppliedEvent`) has grown over time, and each new field uses a different strategy:

| Field | When added | Strategy | Where it lives |
|---|---|---|---|
| `verifiedAddress` (boolean) | v2 | **Calculate** | [`VerifiedAddressUpcaster`](src/main/java/com/example/cqrs/domain/LoanApplication/upcasters/VerifiedAddressUpcaster.java) — derives from `locationType` |
| `currency` (String) | v3 | **Compensate** | [`CurrencyDefaultUpcaster`](src/main/java/com/example/cqrs/domain/LoanApplication/upcasters/CurrencyDefaultUpcaster.java) — defaults to `"EUR"` |
| `manualReviewResult` (String) | v4 | **Enrich Lazily** | [`LoanApplicationHandling.handle(ApproveLoanCommand, …)`](src/main/java/com/example/cqrs/domain/LoanApplication/LoanApplicationHandling.java) |

The first two strategies sit on the **read path**: a pure-function transformation between the store and the application. The third sits on the **write path**: an extra event appended to the entity's stream the first time the entity is touched after the schema change. The hierarchy matters — try Calculate first, then Compensate, only then Enrich.

## Strategy 1 — Calculate via Upcaster

When the missing data can be derived from fields already in the event, an upcaster fills the gap at read time. The event in the store is never touched; the application sees a payload that looks like it was written with the current schema.

[`VerifiedAddressUpcaster`](src/main/java/com/example/cqrs/domain/LoanApplication/upcasters/VerifiedAddressUpcaster.java):

```java
public class VerifiedAddressUpcaster extends AbstractEventDataMarshallingEventUpcaster {

    @Override
    public boolean canUpcast(Event event) {
        if (!event.type().equals(LoanApplicationAppliedEvent.class.getName())) return false;
        Object payload = event.data().get("payload");
        return payload instanceof Map<?, ?> p && !p.containsKey("verifiedAddress");
    }

    @Override
    protected Stream<MetaDataAndPayloadResult> doUpcast(Event event, Map<String, ?> metaData, Map<String, ?> payload) {
        Map<String, Object> upcasted = new HashMap<>(payload);
        upcasted.put("verifiedAddress", "IN_PERSON".equals(payload.get("locationType")));
        return Stream.of(new MetaDataAndPayloadResult(event.type(), metaData, upcasted));
    }
}
```

`canUpcast` is the cheap gate that runs against every event; `doUpcast` only fires for events that genuinely need transformation. The result is the new payload shape — `LoanApplicationAppliedEvent` can have `verifiedAddress` as a required field, and the upcaster guarantees no old event reaches the application without it.

## Strategy 2 — Compensate via Upcaster

When the new field cannot be derived but a meaningful default exists, a compensating upcaster supplies it. Same mechanism, different intent.

[`CurrencyDefaultUpcaster`](src/main/java/com/example/cqrs/domain/LoanApplication/upcasters/CurrencyDefaultUpcaster.java):

```java
@Override
protected Stream<MetaDataAndPayloadResult> doUpcast(Event event, Map<String, ?> metaData, Map<String, ?> payload) {
    Map<String, Object> upcasted = new HashMap<>(payload);
    upcasted.put("currency", "EUR");
    return Stream.of(new MetaDataAndPayloadResult(event.type(), metaData, upcasted));
}
```

The choice of `"EUR"` reflects historical reality: at the time the older events were written, the business operated in a single currency. The default is a faithful reconstruction, not a guess.

## Strategy 3 — Enrich Lazily

When neither Calculate nor Compensate works — there is no derivable source and no meaningful default — the missing data has to be obtained at runtime and persisted as a new event. The first time an entity is touched after the schema change, the handler notices the field is empty, fetches the value, and appends both the enrichment event and the business event in **one** atomic transaction.

[`LoanApplicationHandling.handle(ApproveLoanCommand, …)`](src/main/java/com/example/cqrs/domain/LoanApplication/LoanApplicationHandling.java):

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

Both `publish(…)` calls share one handler invocation and land in ESDB as one append. This delivers three properties that a `CommandRouter`-wrapping gateway with two `send(…)` calls cannot:

- **Atomic** — the enrichment and approval commit together or neither does.
- **OL-compatible** — a `SubjectIsOnEventId` precondition on the incoming command is honoured against the sourced version.
- **Idempotent on retry** — the `if (null)` check against the sourced state guards repeats.

The `ManualReviewService` is the seam to the real-world source — a REST client, gRPC, queue subscription, or human-review system. The [stub](src/main/java/com/example/cqrs/domain/LoanApplication/StubManualReviewService.java) returns `"COMPLIANT"` so the sample is reproducible.

## How the upcasters are wired

Each upcaster is registered as a Spring `@Bean EventUpcaster` in [`OpenCqrsConfig`](src/main/java/com/example/cqrs/configuration/OpenCqrsConfig.java):

```java
@Configuration
public class OpenCqrsConfig {
    @Bean public EventUpcaster verifiedAddressUpcaster(EventDataMarshaller m) { return new VerifiedAddressUpcaster(m); }
    @Bean public EventUpcaster currencyDefaultUpcaster(EventDataMarshaller m) { return new CurrencyDefaultUpcaster(m); }
}
```

OpenCQRS's `EventUpcasterAutoConfiguration` picks up `List<EventUpcaster>` from the context and chains them. Every event read from ESDB passes through `canUpcast`/`upcast` for each registered upcaster — events that are already current short-circuit at `canUpcast == false`, while legacy events get transformed in sequence before they reach the deserializer.

## Commands, events and state

| Type | Role |
|---|---|
| [`ApplyLoanRequestCommand`](src/main/java/com/example/cqrs/domain/LoanApplication/commands/ApplyLoanRequestCommand.java) | `SubjectCondition.PRISTINE` — produces `LoanApplicationAppliedEvent` with all fields populated by the current schema |
| [`ApproveLoanCommand`](src/main/java/com/example/cqrs/domain/LoanApplication/commands/ApproveLoanCommand.java) | `SubjectCondition.EXISTS` — handled by the single-handler enricher |
| [`LoanApplicationAppliedEvent`](src/main/java/com/example/cqrs/domain/LoanApplication/events/LoanApplicationAppliedEvent.java) | The event that grew over time; carries `applicationId`, `applicant`, `amount`, `currency`, `locationType`, `verifiedAddress` |
| [`LoanApplicationEnrichedEvent`](src/main/java/com/example/cqrs/domain/LoanApplication/events/LoanApplicationEnrichedEvent.java) | Written by lazy enrichment; carries `applicationId`, `manualReviewResult` |
| [`LoanApplicationApprovedEvent`](src/main/java/com/example/cqrs/domain/LoanApplication/events/LoanApplicationApprovedEvent.java) | Written by the approve handler |
| [`LoanRequest`](src/main/java/com/example/cqrs/domain/LoanApplication/LoanRequest.java) | Sourced aggregate state — `manualReviewResult` is `null` until the lazy-enrichment branch fires |

## Why not a gateway that sends two commands

A tempting alternative for the third strategy is to wrap `CommandRouter` in a "gateway" that intercepts incoming commands, dispatches an `EnsureLoanEnrichmentCommand` first, then forwards the original. That construction looks elegant but has three structural defects on the command side:

1. **No transaction over the two `send(…)` calls.** If the second send throws, the enrichment is permanent without the approval.
2. **Breaks `SubjectIsOnEventId` optimistic locking.** The gateway appends an event between client-`send` and business handler, moving the subject's tip from version `X` to `Y`. The client's precondition fails.
3. **Idempotency only half-solved.** The enrichment is no-op on replay, but the business command is re-dispatched on retry and needs its own state-based guard.

The single-handler enricher removes all three: one append, one transaction, one precondition check.

## When this is *not* enough

Upcasters and single-handler enrichment work because they live on the read path or inside one atomic append. As soon as the enrichment crosses subjects, takes long enough to be asynchronous, or has its own intermediate state, you outgrow these patterns:

- **Cross-aggregate enrichment** — the enrichment writes to one subject, the business effect to another. No shared atomic append.
- **Asynchronous or long-running enrichment** — external systems that take seconds, retries, human-in-the-loop steps.
- **Multi-step enrichment with intermediate state** — the enrichment itself is a process, not a single decision.

For these, reach for a **Saga** (see the [`implementing-sagas`](../implementing-sagas/) sample). Sagas are designed for orchestrating multiple writes across aggregates with explicit retry, compensation and status tracking — the things an in-handler enricher cannot give you.

## A note on consistency

In OpenCQRS, sourcing reads an entity's full event stream from the store, and writes are committed before `commandRouter.send(…)` returns. **Within a single subject, this is strongly consistent** — after a successful send, the next sourcing on that subject sees the just-written events. Eventual consistency in OpenCQRS applies to projections (read models like `LoanApplicationView`) and to cross-subject reads, not to follow-up command sourcing on the same subject. The single-handler enricher exploits this guarantee directly.

## Running the sample

```bash
docker-compose up -d           # ESDB + Postgres
./gradlew bootRun
```

```bash
# 1. Apply for a loan (new payload — all fields supplied)
curl -X POST http://localhost:8080/api/loan \
     -H 'Content-Type: application/json' \
     -d '{"applicant":"Alice","amount":"10000","currency":"EUR","locationType":"IN_PERSON"}'
# → returns the new applicationId

# 2. Approve it — triggers lazy enrichment + approval in one transaction
curl -X POST http://localhost:8080/api/loan/approve \
     -H 'Content-Type: application/json' \
     -d '{"applicationId":"<id-from-step-1>"}'

# 3. Inspect the read model — verifiedAddress / currency / manualReviewResult all present
curl http://localhost:8080/api/loan/<id-from-step-1>
```

If `currency` or `locationType` are omitted in the apply request, the controller defaults them (`"EUR"` / `"POSTAL"`) — the apply path produces complete events going forward. The upcasters exist for events written before those fields existed in the schema.

## Tests

[`LoanApplicationHandlingTest`](src/test/java/com/example/cqrs/domain/LoanApplication/LoanApplicationHandlingTest.java) covers the command-handling pipeline with `@CommandHandlingTest` and a `@MockitoBean` for `ManualReviewService`:

- new apply produces the full Applied event,
- duplicate apply on existing subject is rejected,
- approve on a not-yet-enriched entity emits **both** enrich and approve events in one append,
- approve on an already-enriched entity emits only the approve event,
- non-`COMPLIANT` review result raises `IllegalStateException`.

[`VerifiedAddressUpcasterTest`](src/test/java/com/example/cqrs/domain/LoanApplication/upcasters/VerifiedAddressUpcasterTest.java) and [`CurrencyDefaultUpcasterTest`](src/test/java/com/example/cqrs/domain/LoanApplication/upcasters/CurrencyDefaultUpcasterTest.java) exercise each upcaster as a pure function over raw `Event`s — proving `canUpcast` and `doUpcast` behaviour without spinning up the framework.
