
# Using (filtered) event streams

---
**NOTE**

This tutorial assumes you have at least completed the official [OpenCQRS-tutorial](https://docs.opencqrs.com/tutorials/) beforehand.

---

A common issue with event-sourcing is that in a long-lived and/or highly utilized app the event-trail with grow to considerable lengths, which can impact performance when a write-model needs to be sourced especially if when only a comparatively small number of events actually pertain to it.

To remedy this, EventSourcingDB provides on with the ability to partition the events contained in the store into smaller streams based on their [subject](https://docs.eventsourcingdb.io/fundamentals/subjects/).

This sample app will showcase, how the OpenCQRS-framework leverages this feature to both model hierarchical relationships between domain entities and source them efficiently inside the business logic.

# The Domain

The domain we use in our sample is a library, with the following two entities used in our write-model:

- `Book`: Represents a specific work available at a library
- `BookCopy`: Represents a physical copy of a Book which can be lent out to readers at the library

Each book can have one or more copies associated with it.

This relationship is reflected in subjects such as:

```
/books/123-4567890/copy/XY
```

When updating the system's write-model for book copy `XY`, the state rebuilding mechanism will *only* consider the events tagged strictly with the above subject.

All other events, such as those pertaining to `/books/123-4567890` in general or any other of its copies are ignored.

## Commands and Events

The 'lifecycle' of a book (copy) in our app looks like this:

![](diagramms/book-lifecycle.svg)

The commands and events in detail:

### [PurchaseBookCommand](src/main/java/com/example/cqrs/domain/api/purchasing/PurchaseBookCommand.java)

The Library purchasing a new copy of a given book. The Subject of the command is of the form:

```
/books/{isbn of book}
```

On success, this yields the following Events:

- `BookInformationAddedEvent`: An entry containing general information about the book is added to the system, iff. it was not present yet.
- `BookCopyAddedEvent`: The purchased copy of the book has been added to the library and made available for lending.

### [BorrowBookCommand](src/main/java/com/example/cqrs/domain/api/borrowing/LendBookCommand.java) and [ReturnBookCommand](src/main/java/com/example/cqrs/domain/api/returning/ReturnBookCommand.java)

A reader borrowing and eventually returning a given copy of the book. The Subject of the commands is of the form:

```
/books/{isbn of book}/copies/{id of copy}
```

On success, they yield a `BookCopyLentEvent` and a `BookCopyReturnedEvent`, respectively
