# Filtering Event Streams

-----

**NOTE**

This tutorial assumes you have completed the official [OpenCQRS-tutorial](https://docs.opencqrs.com/tutorials/).

-----

Event sourcing commonly faces performance issues in long-lived or highly utilized applications due to extensive event trails. This is especially true when only a few events relate to the entity being sourced.

To address this, OpenCQRS leverages one of EventSourcingDB's core features: Tagging events with [subjects](https://docs.eventsourcingdb.io/fundamentals/subjects/).

To summarize, a subject is a string used to associate an event with a specific domain entity. When updating the app's write-model of a given entity, 
OpenCQRS uses the subject to query *only* events [pertaining to that entity](https://docs.eventsourcingdb.io/getting-started/running-eventql-queries/#filtering-by-subject), greatly reducing the amount of events that have to be processed.

Furthermore, subjects can (and should) have a url-like structure which can be used to express hierarchical relationships between domain entities:

![](diagramms/entity-hierarchy.svg)

Our domain for this app is a library with the following two entities:

- `Book`: Represents a specific work available at the library.
- `BookCopy`: Represents a physical copy of a book that can be lent to readers.

Here, there is a clear One-to-Many-relationships between books (the creative work) and their physical copies.

When event-sourcing the write-model for a book with some `ìsbn`, OpenCQRS will consider only the events whose subjects are or start with `/books/{isbn}`,
_including_ events tagged with its copies as their subject, i.e. `/books/{isbn}/copy/{id1}`, `/books/{isbn}/copy/{id2}` etc.

When event-sourcing the write-model for a specific book copy with some `id`, *only* events tagged with the subject `/books/{isbn}/copy/{id}` are queried and all others ignored.

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
