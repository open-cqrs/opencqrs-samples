package com.example.cqrs.domain;

import com.example.cqrs.domain.api.borrowing.LendBookCommand;
import com.example.cqrs.domain.api.returning.ReturnBookCommand;
import com.example.cqrs.domain.api.purchasing.BookCopyAddedEvent;
import com.example.cqrs.domain.api.borrowing.BookCopyLentEvent;
import com.example.cqrs.domain.api.returning.BookCopyReturnedEvent;
import de.dxfrontiers.cqrs.framework.command.CommandEventPublisher;
import de.dxfrontiers.cqrs.framework.command.CommandHandlerConfiguration;
import de.dxfrontiers.cqrs.framework.command.CommandHandling;
import de.dxfrontiers.cqrs.framework.command.StateRebuilding;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@CommandHandlerConfiguration
public class BookCopyHandling {

    @StateRebuilding
    public BookCopy on(BookCopyAddedEvent event) {
        return new BookCopy(event.id());
    }

    @CommandHandling
    public void handle(BookCopy bookCopy, LendBookCommand command, CommandEventPublisher<BookCopy> publisher) {
        if (bookCopy.isLent()) {
            throw new IllegalStateException("Book is already lent!");
        }
        var dueAt = Instant.now().truncatedTo(ChronoUnit.DAYS).plus(30, ChronoUnit.DAYS);

        publisher.publish(
                new BookCopyLentEvent(command.id(), command.isbn(), dueAt)
        );
    }

    @StateRebuilding
    public BookCopy on(BookCopy bookCopy, BookCopyLentEvent event) {
        return bookCopy
                .withDueDate(event.returnDueAt());
    }

    @CommandHandling
    public void handle(BookCopy bookCopy, ReturnBookCommand command, CommandEventPublisher<BookCopy> publisher) {
        if (!bookCopy.isLent()) {
            throw new IllegalStateException("Book is not lent!");
        }

        publisher.publish(
                new BookCopyReturnedEvent(command.id(), command.isbn())
        );
    }

    @StateRebuilding
    public BookCopy on(BookCopyReturnedEvent event) {
        return new BookCopy(event.id());
    }
}
