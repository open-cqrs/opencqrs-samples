package com.example.cqrs.domain;

import com.example.cqrs.domain.api.command.EditBookDetailsCommand;
import com.example.cqrs.domain.api.command.PurchaseBookCommand;
import com.example.cqrs.domain.api.event.BookDetailsEditedEvent;
import com.example.cqrs.domain.api.event.BookPurchasedEvent;
import com.opencqrs.esdb.client.Event;
import com.opencqrs.framework.command.*;

import java.util.List;

@CommandHandlerConfiguration
public class BookHandling {

    @CommandHandling(sourcingMode = SourcingMode.LOCAL)
    public void handle(PurchaseBookCommand cmd, CommandEventPublisher<Book> publisher) {
        publisher.publish(new BookPurchasedEvent(cmd.isbn(), cmd.title(), cmd.authors()));
    }

    @StateRebuilding
    public Book on(BookPurchasedEvent e, Event rawEvent) {
        return new Book(rawEvent, e.isbn(), e.title(), List.copyOf(e.authors()));
    }

    @CommandHandling
    public void handle(Book book, EditBookDetailsCommand cmd, CommandEventPublisher<Book> publisher) {
        cmd.assertVersionMatches(book);
        if (book.title().equals(cmd.title()) && book.authors().equals(cmd.authors()))
            return;
        publisher.publish(new BookDetailsEditedEvent(cmd.isbn(), cmd.title(), cmd.authors()));
    }

    @StateRebuilding
    public Book on(Book book, BookDetailsEditedEvent e, Event rawEvent) {
        return new Book(rawEvent, book.isbn(), e.title(), List.copyOf(e.authors()));
    }
}
