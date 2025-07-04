# Filtering Event Streams

-----

**NOTE**

This tutorial assumes you have completed the official [OpenCQRS-tutorial](https://docs.opencqrs.com/tutorials/).

-----

Event sourcing commonly faces performance issues in long-lived or highly utilized applications due to extensive event trails. This is especially true when only a few events relate to the entity being sourced.

To address this, OpenCQRS leverages one of EventSourcingDB's core features: Tagging events with [subjects](https://docs.eventsourcingdb.io/fundamentals/subjects/).

## What is a Subject?

A **subject** is a string that uniquely identifies the domain entity (or a group of related entities) that an event belongs to.  
Subjects in EventSourcingDB act as **indexable tags** that allow highly efficient filtering of event streams.

A subject should follow a **URL-like, hierarchical structure** to naturally reflect relationships in your domain model.

### Example: Library Domain
In our library application, we have two main entities:
- `Book`: A work identified by an ISBN.
- `BookCopy`: A physical, borrowable copy of a book.

We use the following subject conventions:
- For a book:
  ```
  /books/{isbn}
  ```
- For a copy of a book:
  ```
  /books/{isbn}/copies/{copyId}
  ```

Here is a clear One-to-Many-relationships between books (the creative work) and their physical copies.


## Hierarchical Indexing in EventSourcingDB

EventSourcingDB automatically creates a **hierarchical index** on the `subject` field.  
This enables two powerful query patterns:
1. **Prefix Queries:** You can efficiently load all events for a parent entity and its children using subject prefixes.
2. **Exact Queries:** You can filter events for a specific sub-entity using the full subject path.

### Example Queries:
1. When rebuilding the write model for a book with ISBN `12345`, OpenCQRS will query **all events whose subjects start with**:
   ```
   /books/12345
   ```
   This includes:
   ```
   /books/12345
   /books/12345/copies/1
   /books/12345/copies/2
   ```
   ➜ This efficiently loads both book-level and book copy events in a single    stream.

2. When rebuilding the write model for a specific book copy with ID 1, OpenCQRS will query only events with subject:
   ```
   /books/12345/copies/1
   ```
   
   ➜ This isolates the event stream to the relevant copy, ignoring all others.


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

To run the app, ensure you have [Docker](https://www.docker.com/) installed on your system as well as being logged into the [GitHub Container Registry](https://docs.github.com/de/packages/working-with-a-github-packages-registry/working-with-the-container-registry#authentifizieren-bei-der-container-registry).

Then run:

```bash
docker-compose up
```

This command will start:

- An instance of EventSourcingDB.
- An instance of the app itself.

To interact with the app, we provide a [collection](clients) of requests for the [Postman](https://www.postman.com/) and [Bruno](https://www.usebruno.com/) API clients.
