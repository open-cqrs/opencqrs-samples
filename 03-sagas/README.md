
# Introduction

A common requirement occurring in distributed systems is the need to validate a request against a multitude of different services instead of just one.

Such long-running, multi-system transactions are commonly referred to as _Sagas_, and in the context of CQRS+ES, they take the form of commands whose validation crosses the boundaries of multiple subjects.

The purpose of this app is to give an example of how to model such a transaction using the OpenCQRS-framework.

# Motivation

Our domain is a library, where we have two principal entities:

- Readers (identified by an ID)
- Books (identified by their ISBN)

We now want to implement the process of a reader lending a book with two constraints:

- A reader can at any time have at most two books lent out to them
- A book can only be lent out to exactly one reader

The first has to be validated against the write-model of the reader, the second one against the write-model of the book.

A naive implementation without a dedicated 'coordinator', i.e. with the involved entities simply exchanging messages between each other, could look something like this:

![](diagrams/naive-solution.svg)

Here, we annotated each command and event with the entity information available and needed to successfully process them. And through this, we instantly see the problem with this approach:

When trying to send back a `Receive Book`-command for the reader lending the book, we need their ID. However, since the book's `Reserve Book`-command does _not_ have the reader ID available (since there is no reason for it to from a business-logic perspective) this information is missing now (thus marked red).

Technically, this problem can be easily solved by just propagating the reader ID through the entire workflow, i.e. also providing it to `Reserve Book` and its subsequent event, 
but that would explicitly couple the book's domain-logic with the reader's for purely technical reasons.

For such a simple domain and use case, this might not seem like a big issue but once one has to implement more sophisticated transactions involving more entities or subjects, having _all_ information that may be
needed at _some_ point be propagated throughout the _entire_ process can not only become unwieldy, it compromises one of the key concepts of Domain-Driven Design: Having clear, well-defined boundaries between a domain's contexts and the entities within them.

# Sagas in (Open)CQRS

To remedy the issues outlined above, one can utilize a central coordinator (also sometimes referred to as an orchestrator) that sits between/above the involved entities of the saga.

In terms of the OpenCQRS-framework , this would be a bean being instantiated for each individual lending process which both persists the correlation between the reader (ID) and book (ISBN) in question,
while using `@EventHandling`-annotated methods to listen to events pertaining to one subject to the issue commands to the other.

Using regular `@Component`- or `@Service`-beans comes with two issues on its own:

- They only exist in memory, thus the reader/book-correlation (realized via fields inside the bean) would be lost in case of an erroneous JVM-shutdown, requiring some further external infrastructure (such as a DB or Redis) for persistence
- OpenCQRS-Eventhandlers require their parent classes to be singleton beans (Spring's default), whereas saga-beans would have to be instantiated on a per-request basis (i.e. request-scoped).

Thus, the approach chosen here is to model saga-coordinators/orchestrators as their own subject alongside the existing domain-entities. In specific case, we call this new subject-type: `Loan`.

## Happy Paths

In the case of a successful loan, with no errors happening, our saga will look like this:

![](diagrams/happy-path-reader.svg)

- We start with an initializing command which creates the loan subject-instance for the given reader-ID/book-ISBN combination.
- The corresponding event is handled by issuing the appropriate command under the reader's subject
- The command is validated (reader hasn't reached their limit of lent books), the reader's write-model, it's corresponding event is fired and the reader's write-model updated with a reference to the loan
- Said event is handled by issuing a command back under the loan's subject, which kicks of the saga's validation on the book's side:

![](diagrams/happy-path-book.svg)

The logic is basically identical to the reader's side of the transaction, with the book's write-model also being updated with a reference to its new, currently active loan.

[Discuss idempotency here?]

[Discuss dependency on loan ID/ref here?]

## Error Paths

Naturally, commands can also be rejected and the lending process fail as a consequence.

For these cases, we also have to think about how to roll back the 'dirty' state of our system from the midst of an ongoing transaction to a clean one, ready for new commands.

If the command handler of the reader's `Add Loan`-command rejects (signified by return value `false`), due to an already reached limit of active loans, the rollback is straight-forward since no state-changing events have been fired yet.
Simply issue a `Cancel Loan`-command and be done:

![](diagrams/error-path-reader.svg)

Things are more complicated, if the book's `Reserve Book`-command rejects, because the book in question is already being reserved by another active loan: At this point in the transaction, the reader's write-model has already been updated
with a reference to the loan being processed via the `Loan Added`-event. Said event must now be _compensated_, before cancelling the lending process altogether:

![](diagrams/error-path-book-1.svg)

![](diagrams/error-path-book-2.svg)