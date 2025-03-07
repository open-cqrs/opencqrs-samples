
# Subject Hierarchies

This sample app will showcase, how our framework handles event sourcing in the context of hierarchically dependent domain entities:

- Books
- Rentals

Specifically, the framework allows us to source different write-models of our entities from different (smaller) subsets of the event trail, depending on whether we need information only about a specific rental or the book as a whole.

## Commands/Events

For the purpose of this app, we introduce three basic actions, each of which is represented by exactly one Command and Event

- The Library purchasing a book (Subject: "/books/{ISBN of the book}")
- A reader renting a book (Subject: "/books/{ISBN of the book}/rentals/{ID of the rental process}")
- A reader returning a lent book (Subject: "/books/{ISBN of the book}/rentals/{ID of the rental process}")

## Domain Entities

The first Domain entity, which we track as subjects in our commands/events are Books. The Book-class contains information such as a book's ISBN, title and author.

The second entity are Rentals. The Rental-class tracks the ID of the renting reader, the due date of the rental and the state of a renting process (active, overdue, returned)