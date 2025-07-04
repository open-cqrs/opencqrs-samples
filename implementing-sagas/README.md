# Implementing Sagas

-----

**NOTE**

This tutorial assumes you have completed the official [OpenCQRS-tutorial](https://docs.opencqrs.com/tutorials/).

-----

Distributed systems often need to validate a request against multiple services, not just one.

These long-running, multi-system transactions are known as **Sagas**. In a CQRS+ES context, sagas involve commands whose validation spans multiple subjects.

This app demonstrates how to model such a transaction using the OpenCQRS framework.

# Motivation

Consider a library domain with two main entities:

* **Readers** (identified by an ID)
* **Books** (identified by their ISBN)

We want to implement a book lending process with two constraints:

* A reader can have a maximum of two books lent at any time.
* A book can be lent to only one reader at a time.

The first constraint requires validation against the reader's write-model, while the second requires validation against the book's write-model.

A naive implementation, where entities exchange messages without a dedicated coordinator, might look like this:

![](diagrams/naive-solution.svg)

Each command and event is annotated with the necessary entity information. This highlights a problem:

To send a `Receive Book` command for the reader lending the book, we need the reader's ID. However, the book's `Reserve Book` command does not have the reader ID (as there's no business logic reason for it), so this information is missing (marked red).

Theoretically, propagating the reader ID throughout the workflow (i.e., providing it to `Reserve Book` and its subsequent event) would solve this. However, this couples the book's domain logic with the reader's for purely technical reasons.

For a simple domain and use case, this might seem minor. But in more complex transactions involving a multitude of entities or subjects, propagating *all* potentially needed information throughout the *entire* process becomes unwieldy. It also compromises a key Domain-Driven Design principle: maintaining clear, well-defined boundaries between a domain's contexts and its entities.

# Sagas in (Open)CQRS

To address these issues, use a central **coordinator** (or orchestrator) that sits between or above the saga's involved entities.

In the OpenCQRS framework, we achieve this by introducing a third subject type: the [Loan](src/main/java/com/example/cqrs/domain/Loan.java).
This leverages the event store to persist the correlation between a reader's ID and a book's ISBN, while allowing new loan entities to be instantiated for each lending process.

The actual coordination happens in the [LoanHandling](src/main/java/com/example/cqrs/domain/LoanHandling.java) class, where various `@EventHandling`-annotated methods dispatch commands to a loan's associated reader and book subjects.

-----

**NOTE**

One might consider using regular `@Component` or `@Service` beans as coordinators, as introducing a new write-model subject with its own commands and events (and subsequent handlers) adds some overhead.

However, this approach wouldn't work for two reasons:

* Beans exist only in memory. Reader/book correlation (via fields in the bean) would be lost in case of a JVM shutdown, requiring external infrastructure (like a DB or Redis) for persistence.
* `@EventHandling` methods require their parent classes to be singleton beans (Spring's default), whereas saga beans would need to be instantiated per-request (i.e., request-scoped).

-----

## Happy Paths

In a successful loan scenario, with no errors, our saga will look like this:

![](diagrams/happy-path-reader.svg)

* The process begins with an initializing command that creates the loan subject instance for the given reader ID and book ISBN.
* The corresponding event is handled by issuing the appropriate command under the reader's subject.
* The command is validated (e.g., the reader hasn't reached their book limit), the reader's write-model is updated, its corresponding event is fired, and the reader's write-model is updated with a reference to the loan.
* This event is then handled by issuing a command back under the loan's subject, initiating the saga's validation on the book's side:

![](diagrams/happy-path-book.svg)

The logic is essentially identical to the reader's side of the transaction, with the book's write-model also being updated with a reference to its new, currently active loan.

## Error Paths

Commands can be rejected, causing the lending process to fail.

In such cases, we must also consider how to roll back the system's "dirty" state from an ongoing transaction to a clean state, ready for new commands.

If the reader's `Add Loan` command handler rejects (returns `false`) due to an already reached limit of active loans, 
the rollback is straightforward since no state-changing events have been fired yet. Simply issue a `Cancel Loan` command:

![](diagrams/error-path-reader.svg)

Things are more complicated if the book's `Reserve Book` command rejects because the book is already reserved by another active loan. 
At this point, the reader's write-model has already been updated with a reference to the loan being processed via the `Loan Added` event. 
This event must now be **compensated** before canceling the lending process entirely:

![](diagrams/error-path-book-1.svg)

![](diagrams/error-path-book-2.svg)

In each case, the transaction ends in a clean state where both subjects' constraints are preserved.

## Running the App

To run the app, ensure you have [Docker](https://www.docker.com/) installed on your system as well as being logged into the [GitHub Container Registry](https://docs.github.com/de/packages/working-with-a-github-packages-registry/working-with-the-container-registry#authentifizieren-bei-der-container-registry).

Then run:

```bash
docker-compose up
```

This command will start:

- An instance of EventSourcingDB.
- An instance of the app itself.

To interact with the app, we provide a [collection](clients) of requests for the [Postman](https://www.postman.com/) and [Bruno](https://www.usebruno.com/) API clients.