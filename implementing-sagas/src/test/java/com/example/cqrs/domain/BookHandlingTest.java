package com.example.cqrs.domain;

import com.example.cqrs.domain.api.purchase.BookPurchasedEvent;
import com.example.cqrs.domain.api.rental.BookReservedEvent;
import com.example.cqrs.domain.api.rental.ReserveBookCommand;
import com.opencqrs.framework.command.CommandHandlingTest;
import com.opencqrs.framework.command.CommandHandlingTestFixture;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

/**
 * For a tutorial on testing in OpenCQRS
 *
 * @see https://docs.opencqrs.com/tutorials/06_testing/
 */
@CommandHandlingTest
public class BookHandlingTest {

    @Test
    public void willReserveBook(@Autowired CommandHandlingTestFixture<Book, ReserveBookCommand, Boolean> fixture) {

        var loanId = UUID.randomUUID();
        var isbn = "012-34567890";

        fixture
                .given(
                        new BookPurchasedEvent(isbn, "Title", "A. Uthor")
                ).when(
                        new ReserveBookCommand(loanId, isbn)
                ).expectResult(
                        true
                ).expectSingleEvent(
                        new BookReservedEvent(loanId, isbn)
                );
    }

    @Test
    public void willIgnoreDuplicateReservation(@Autowired CommandHandlingTestFixture<Book, ReserveBookCommand, Boolean> fixture) {

        var loanId = UUID.randomUUID();
        var isbn = "012-34567890";

        fixture
                .given(
                        new BookPurchasedEvent(isbn, "Title", "A. Uthor"),
                        new BookReservedEvent(loanId, isbn)
                ).when(
                        new ReserveBookCommand(loanId, isbn)
                ).expectResult(
                        true
                ).expectNoEvents();
    }

    @Test
    public void willRejectReservation(@Autowired CommandHandlingTestFixture<Book, ReserveBookCommand, Boolean> fixture) {

        var loanId1 = UUID.randomUUID();
        var loanId2 = UUID.randomUUID();
        var isbn = "012-34567890";

        fixture
                .given(
                        new BookPurchasedEvent(isbn, "Title", "A. Uthor"),
                        new BookReservedEvent(loanId1, isbn)
                ).when(
                        new ReserveBookCommand(loanId2, isbn)
                ).expectResult(
                        false
                ).expectNoEvents();
    }
}
