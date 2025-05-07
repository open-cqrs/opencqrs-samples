package com.example.cqrs.domain;

import com.example.cqrs.domain.api.purchase.BookPurchasedEvent;
import com.example.cqrs.domain.api.purchase.PurchaseBookCommand;
import com.example.cqrs.domain.api.rental.BookReservedEvent;
import com.example.cqrs.domain.api.rental.CompleteLoanCommand;
import com.example.cqrs.domain.api.rental.ReserveBookCommand;
import com.opencqrs.framework.command.*;
import com.opencqrs.framework.eventhandler.EventHandling;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@CommandHandlerConfiguration
public class BookHandling {

    @CommandHandling
    public void handle(Book book, PurchaseBookCommand command, CommandEventPublisher<Book> publisher) {
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
    public boolean handle(Book book, ReserveBookCommand command, CommandEventPublisher<Book> publisher) {
        if (!book.isLent()) {
            publisher.publish(new BookReservedEvent(command.loanId(), command.isbn()));
            return true;
        } else {
            return false;
        }
    }

    @StateRebuilding
    public Book on(Book book, BookReservedEvent event) {
        return book.withDueDate(Instant.now().plus(30, ChronoUnit.DAYS));
    }

    @EventHandling("loan")
    public void on(BookReservedEvent event, @Autowired CommandRouter router) {
        router.send(new CompleteLoanCommand(event.loanId()));
    }
}
