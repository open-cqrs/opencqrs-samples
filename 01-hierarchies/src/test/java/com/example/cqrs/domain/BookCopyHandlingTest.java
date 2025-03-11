package com.example.cqrs.domain;

import com.example.cqrs.domain.api.borrowing.BookCopyLentEvent;
import com.example.cqrs.domain.api.borrowing.BorrowBookCommand;
import com.example.cqrs.domain.api.purchasing.PurchaseBookCommand;
import com.example.cqrs.domain.api.returning.BookCopyReturnedEvent;
import com.example.cqrs.domain.api.returning.ReturnBookCommand;
import com.example.cqrs.services.UUIDGeneratorService;
import de.dxfrontiers.cqrs.framework.command.CommandHandlingTest;
import de.dxfrontiers.cqrs.framework.command.CommandHandlingTestFixture;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@CommandHandlingTest
public class BookCopyHandlingTest {

    @MockitoBean
    private UUIDGeneratorService uuidGen;

    @Test
    public void willLendBookCopy(@Autowired CommandHandlingTestFixture<BookCopy, BorrowBookCommand, Void> fixture) {

        var id = UUID.randomUUID();
        var isbn = "012-34567890";
        var dueAt = Instant.now().truncatedTo(ChronoUnit.DAYS).plus(30, ChronoUnit.DAYS);

        fixture
                .givenState(
                        new BookCopy(id, null, false)
                )
                .when(
                        new BorrowBookCommand(id, isbn)
                )
                .expectSuccessfulExecution()
                .expectSingleEvent(
                        new BookCopyLentEvent(id, isbn, dueAt)
                )
                .expectState(
                        new BookCopy(id, dueAt, true)
                );
    }

    @Test
    public void willNotLendAlreadyLentCopy(@Autowired CommandHandlingTestFixture<BookCopy, BorrowBookCommand, Void> fixture) {

        var id = UUID.randomUUID();
        var isbn = "012-34567890";
        var dueAt = Instant.now().truncatedTo(ChronoUnit.DAYS).plus(30, ChronoUnit.DAYS);

        fixture
                .givenState(
                        new BookCopy(id, dueAt, true)
                )
                .when(
                        new BorrowBookCommand(id, isbn)
                )
                .expectUnsuccessfulExecution();
    }

    @Test
    public void willReturnBookCopy(@Autowired CommandHandlingTestFixture<BookCopy, ReturnBookCommand, Void> fixture) {

        var id = UUID.randomUUID();
        var isbn = "012-34567890";
        var dueAt = Instant.now().truncatedTo(ChronoUnit.DAYS).plus(30, ChronoUnit.DAYS);

        fixture
                .givenState(
                        new BookCopy(id, dueAt, true)
                )
                .when(
                        new ReturnBookCommand(id, isbn)
                )
                .expectSuccessfulExecution()
                .expectSingleEvent(
                        new BookCopyReturnedEvent(id, isbn)
                )
                .expectState(
                        new BookCopy(id, null, false)
                );
    }

    @Test
    public void willNotReturnBookCopyThatIsNotLent(@Autowired CommandHandlingTestFixture<BookCopy, ReturnBookCommand, Void> fixture) {

        var id = UUID.randomUUID();
        var isbn = "012-34567890";
        var dueAt = Instant.now().truncatedTo(ChronoUnit.DAYS).plus(30, ChronoUnit.DAYS);

        fixture
                .givenState(
                        new BookCopy(id, null, false)
                )
                .when(
                        new ReturnBookCommand(id, isbn)
                )
                .expectUnsuccessfulExecution();
    }
}
