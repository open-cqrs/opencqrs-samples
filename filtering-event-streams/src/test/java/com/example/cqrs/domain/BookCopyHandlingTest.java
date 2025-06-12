package com.example.cqrs.domain;

import com.example.cqrs.domain.api.borrowing.BookCopyLentEvent;
import com.example.cqrs.domain.api.borrowing.LendBookCommand;
import com.example.cqrs.domain.api.purchasing.BookCopyAddedEvent;
import com.example.cqrs.domain.api.returning.BookCopyReturnedEvent;
import com.example.cqrs.domain.api.returning.ReturnBookCommand;
import com.opencqrs.framework.command.CommandHandlingTest;
import com.opencqrs.framework.command.CommandHandlingTestFixture;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * @see <a href="https://docs.opencqrs.com/tutorials/06_testing/">Tutorial on testing in OpenCQRS</a>
 */
@CommandHandlingTest
public class BookCopyHandlingTest {

    @Test
    public void willLendBookCopy(@Autowired CommandHandlingTestFixture<BookCopy, LendBookCommand, Void> fixture) {

        var id = UUID.randomUUID();
        var isbn = "012-34567890";
        var dueAt = Instant.now().truncatedTo(ChronoUnit.DAYS).plus(30, ChronoUnit.DAYS);

        fixture
                .given(
                        new BookCopyAddedEvent(id)
                )
                .when(
                        new LendBookCommand(id, isbn)
                )
                .expectSuccessfulExecution()
                .expectSingleEvent(
                        new BookCopyLentEvent(id, isbn, dueAt)
                );
    }

    @Test
    public void willNotLendAlreadyLentCopy(@Autowired CommandHandlingTestFixture<BookCopy, LendBookCommand, Void> fixture) {

        var id = UUID.randomUUID();
        var isbn = "012-34567890";
        var dueAt = Instant.now().truncatedTo(ChronoUnit.DAYS).plus(30, ChronoUnit.DAYS);

        fixture
                .given(
                        new BookCopyAddedEvent(id),
                        new BookCopyLentEvent(id, isbn, dueAt)
                )
                .when(
                        new LendBookCommand(id, isbn)
                )
                .expectException(IllegalStateException.class);
    }

    @Test
    public void willReturnBookCopy(@Autowired CommandHandlingTestFixture<BookCopy, ReturnBookCommand, Void> fixture) {

        var id = UUID.randomUUID();
        var isbn = "012-34567890";
        var dueAt = Instant.now().truncatedTo(ChronoUnit.DAYS).plus(30, ChronoUnit.DAYS);

        fixture
                .given(
                        new BookCopyAddedEvent(id),
                        new BookCopyLentEvent(id, isbn, dueAt)
                )
                .when(
                        new ReturnBookCommand(id, isbn)
                )
                .expectSuccessfulExecution()
                .expectSingleEvent(
                        new BookCopyReturnedEvent(id, isbn)
                );
    }

    @Test
    public void willNotReturnBookCopyThatIsNotLent(@Autowired CommandHandlingTestFixture<BookCopy, ReturnBookCommand, Void> fixture) {

        var id = UUID.randomUUID();
        var isbn = "012-34567890";

        fixture
                .given(
                        new BookCopyAddedEvent(id)
                )
                .when(
                        new ReturnBookCommand(id, isbn)
                )
                .expectException(IllegalStateException.class);
    }
}
