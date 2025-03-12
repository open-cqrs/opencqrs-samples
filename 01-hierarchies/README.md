
# Sample 01 - Hierarchical Domains and filtered Event-Streams

This sample app will showcase, how our framework handles event sourcing in the context of hierarchically dependent domain entities:

- `Book`: Represents a specific work available at a library
- `BookCopy`: Represents a physical copy of a Book which can be lent out to readers at the library

Each book can have one or more copies associated with it.

In the OpenCQRS-Framework, such entity relationships are reflected by a command's subject, which are url-like strings which can represent hierarchical relationships.

Using this mechanism, the state aggregation mechanism is able to filter for and source the specific events relevant to the write-model instance in question, e.g.

When lending a copy XY of the book with ISBN 123-4567890, updating the write-model's state for copy XY *only* requires the sourcing of the events with subject:

```
/books/123-4567890/copy/XY
```

All other events, such as those pertaining to `/books/123-4567890` in general or any other of its copies are ignored.

When dealing with long event-streams, this will noticeably decrease the amount of events that will have to be loaded, increasing performance.

## Commands and Events

For the purpose of this app, we introduce a set of basic commands with corresponding events:

### PurchaseBookCommand

The Library purchasing a new copy of a given book. The Subject of the command is of the form:

```
/books/{isbn of book}
```

On success, this yields the following Events:

- `BookInformationAddedEvent`: An entry containing general information about the book is added to the system, iff. it was not present yet.
- `BookCopyAddedEvent`: The purchased copy of the book has been added to the library and made available for lending.

### BorrowBookCommand and ReturnBookCommand

A reader borrowing and eventually returning a given copy of the book. The Subject of the commands is of the form:

```
/books/{isbn of book}/copies/{id of copy}
```

On success, they yield a `BookCopyLentEvent` and a `BookCopyReturnedEvent`, respectively
