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
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@CommandHandlerConfiguration
public class BookHandling {

    @CommandHandling
    public void handle(PurchaseBookCommand command, CommandEventPublisher<Void> publisher) {
        publisher.publish(
                new BookPurchasedEvent(
                        command.isbn(),
                        command.title(),
                        command.author()
                )
        );
    }

    @StateRebuilding
    public Book on(BookPurchasedEvent event) {
        return new Book(event.isbn());
    }

    @CommandHandling
    public Instant handle(Book book, LendBookCommand command, CommandEventPublisher<Book> publisher) {
        if (book.isLent()) throw new IllegalStateException("book currently lent");

        var dueAt = Instant.now().truncatedTo(ChronoUnit.DAYS).plus(30, ChronoUnit.DAYS);

        publisher.publish(new BookLentEvent(command.readerId(), command.bookISBN(), dueAt));

        return dueAt;
    }

    @StateRebuilding
    public Book on(BookLentEvent event) {
        return new Book(event.bookISBN(), event.dueAt());
    }

    @CommandHandling
    public void handle(
            Book book,
            ReturnBookCommand command,
            CommandEventPublisher<Book> publisher
    ) {
        if (!book.isLent()) throw new IllegalStateException("book currently not lent");

        publisher.publish(new BookReturnedEvent(command.readerId(), command.bookISBN()));
    }

    @StateRebuilding
    public Book on(BookReturnedEvent event) {
        return new Book(event.bookISBN());
    }
}
