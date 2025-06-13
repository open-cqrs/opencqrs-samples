# Using (Filtered) Event Streams

-----

**NOTE**

This tutorial assumes you have completed the official [OpenCQRS-tutorial](https://docs.opencqrs.com/tutorials/).

-----

Event sourcing commonly faces performance issues in long-lived or highly utilized applications due to extensive event trails. This is especially true when only a few events relate to the entity being sourced.

To address this, EventSourcingDB partitions events into smaller streams based on their [subject](https://docs.eventsourcingdb.io/fundamentals/subjects/). This sample app demonstrates how the OpenCQRS framework uses this feature to model hierarchical relationships between domain entities and source them efficiently.

# The Domain

Our sample uses a library domain with the following two entities in its write-model:

- `Book`: Represents a specific work available at the library.
- `BookCopy`: Represents a physical copy of a book that can be lent to readers.

Each book can have one or more associated copies.

This relationship is reflected in subjects such as:

```
/books/123-4567890/copy/XY
```

When updating the system's write-model for book copy `XY`, the state rebuilding mechanism *only* considers events strictly tagged with the above subject. Other events, such as those pertaining to `/books/123-4567890` in general or any other copies, are ignored.

## Commands and Events

The lifecycle of a book (copy) in our app is depicted below:

![](diagramms/book-lifecycle.svg)

The commands and events are detailed as follows:

### [PurchaseBookCommand](src/main/java/com/example/cqrs/domain/api/purchasing/PurchaseBookCommand.java)

The library purchases a new copy of a given book. The command's subject is in the format:

```
/books/{isbn of book}
```

Upon success, this yields the following events:

- `BookInformationAddedEvent`: An entry with general book information is added to the system if not already present.
- `BookCopyAddedEvent`: The purchased book copy is added to the library and made available for lending.

### [BorrowBookCommand](src/main/java/com/example/cqrs/domain/api/borrowing/LendBookCommand.java) and [ReturnBookCommand](src/main/java/com/example/cqrs/domain/api/returning/ReturnBookCommand.java)

A reader borrows and eventually returns a specific copy of a book. The commands' subject is in the format:

```
/books/{isbn of book}/copies/{id of copy}
```

Upon success, they yield a `BookCopyLentEvent` and a `BookCopyReturnedEvent`, respectively.

# Running the App

To run the app, ensure you have [Docker](https://www.docker.com/) installed on your system.

Then run:

```bash
docker-compose up
```

This command will start:

- An instance of EventSourcingDB.
- An instance of the app itself.

To interact with the app, we provide a [collection](clients) of requests for the [Postman](https://www.postman.com/) and [Bruno](https://www.usebruno.com/) API clients.
