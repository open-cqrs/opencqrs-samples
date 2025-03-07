package com.opencqrs.domain;

import com.opencqrs.domain.api.borrowing.BorrowBookCommand;
import com.opencqrs.domain.api.returning.ReturnBookCommand;
import com.opencqrs.domain.api.purchasing.BookCopyAddedEvent;
import com.opencqrs.domain.api.borrowing.BookCopyLentEvent;
import com.opencqrs.domain.api.returning.BookCopyReturnedEvent;
import de.dxfrontiers.cqrs.framework.command.CommandEventPublisher;
import de.dxfrontiers.cqrs.framework.command.CommandHandlerConfiguration;
import de.dxfrontiers.cqrs.framework.command.CommandHandling;
import de.dxfrontiers.cqrs.framework.command.StateRebuilding;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;

@CommandHandlerConfiguration
public class BookCopyHandling {

    private static final Random random = new Random();

    @StateRebuilding
    public BookCopy on(BookCopyAddedEvent event) {
        return new BookCopy(event.id(), null, false);
    }

    @CommandHandling
    public void handle(BookCopy bookCopy, BorrowBookCommand command, CommandEventPublisher<Book> publisher) {
        if (bookCopy.isLent()) {
            throw new IllegalStateException("Book is already lent!");
        }
        var dueAt = Instant.now().plus(random.ints(7, 30).findFirst().getAsInt(), ChronoUnit.DAYS);

        publisher.publish(
                new BookCopyLentEvent(command.id(), command.isbn(), dueAt)
        );
    }

    @StateRebuilding
    public BookCopy on(BookCopy bookCopy, BookCopyLentEvent event) {
        return bookCopy.withRentalStatus(true);
    }

    @CommandHandling
    public void handle(BookCopy bookCopy, ReturnBookCommand command, CommandEventPublisher<Book> publisher) {
        if (!bookCopy.isLent()) {
            throw new IllegalStateException("Book is not lent!");
        }

        publisher.publish(
                new BookCopyReturnedEvent(command.id(), command.isbn())
        );
    }

    @StateRebuilding
    public BookCopy on(BookCopy bookCopy, BookCopyReturnedEvent event) {
        return bookCopy.withRentalStatus(false);
    }
}
