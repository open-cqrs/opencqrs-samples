package com.example.cqrs.domain;

import com.example.cqrs.domain.api.purchase.BookPurchasedEvent;
import com.example.cqrs.domain.api.purchase.PurchaseBookCommand;
import com.example.cqrs.domain.api.rental.BookLentEvent;
import com.example.cqrs.domain.api.rental.BookReturnedEvent;
import com.example.cqrs.domain.api.rental.LendBookCommand;
import com.example.cqrs.domain.api.rental.ReturnBookCommand;
import com.opencqrs.framework.command.CommandEventPublisher;
import com.opencqrs.framework.command.CommandHandlerConfiguration;
import com.opencqrs.framework.command.CommandHandling;
import com.opencqrs.framework.command.StateRebuilding;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@CommandHandlerConfiguration
public class BookHandling {

    @CommandHandling
    public void handle(Book book, PurchaseBookCommand command, CommandEventPublisher<Book> publisher) {
        if (book == null) {
            publisher.publish(
                    new BookPurchasedEvent(
                            command.isbn(),
                            command.title(),
                            command.author()
                    )
            );
        } else {
            throw new IllegalStateException("Book already in stock");
        }
    }

    @StateRebuilding
    public Book on(BookPurchasedEvent event) {
        return new Book(event.isbn());
    }

    @CommandHandling
    public void handle(
            Book book,
            LendBookCommand command,
            Map<String, String> metadata,
            CommandEventPublisher<Book> publisher
    ) {
        if (book.isLent()) throw new IllegalStateException("book currently lent");
        if (metadata == null || !metadata.containsKey("correlation-id")) throw new IllegalStateException("no correlation id provided");

        var dueAt = Instant.now().truncatedTo(ChronoUnit.DAYS).plus(30, ChronoUnit.DAYS);

        publisher.publish(new BookLentEvent(command.id(), command.isbn(), dueAt), metadata);
    }

    @StateRebuilding
    public Book on(BookLentEvent event) {
        return new Book(event.isbn(), event.dueAt());
    }

    @CommandHandling
    public void handle(
            Book book,
            ReturnBookCommand command,
            Map<String, String> metadata,
            CommandEventPublisher<Book> publisher
    ) {
        if (!book.isLent()) throw new IllegalStateException("book currently not lent");
        if (metadata == null || !metadata.containsKey("correlation-id")) throw new IllegalStateException("no correlation id provided");

        publisher.publish(new BookReturnedEvent(command.id(), command.isbn()), metadata);
    }

    @StateRebuilding
    public Book on(BookReturnedEvent event) {
        return new Book(event.isbn());
    }
}
