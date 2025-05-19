package com.example.cqrs.domain;

import com.example.cqrs.domain.api.registration.ReaderRegisteredEvent;
import com.example.cqrs.domain.api.rental.AddLoanToReaderCommand;
import com.example.cqrs.domain.api.rental.LoanAddedToReaderEvent;
import com.opencqrs.framework.command.CommandHandlingTest;
import com.opencqrs.framework.command.CommandHandlingTestFixture;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

@CommandHandlingTest
public class ReaderHandlingTest {

    @Test
    public void willAddLoan(@Autowired CommandHandlingTestFixture<Reader, AddLoanToReaderCommand, Boolean> fixture) {

        var readerId = UUID.randomUUID();
        var loanId = UUID.randomUUID();

        fixture
                .given(
                        new ReaderRegisteredEvent(readerId, "Max", "Mustermann")
                ).when(
                        new AddLoanToReaderCommand(loanId, readerId)
                ).expectResult(
                        true
                ).expectSingleEvent(
                        new LoanAddedToReaderEvent(loanId, readerId)
                );
    }

    @Test
    public void willIgnoreDuplicateLoan(@Autowired CommandHandlingTestFixture<Reader, AddLoanToReaderCommand, Boolean> fixture) {

        var readerId = UUID.randomUUID();
        var loanId = UUID.randomUUID();

        fixture
                .given(
                        new ReaderRegisteredEvent(readerId, "Max", "Mustermann"),
                        new LoanAddedToReaderEvent(loanId, readerId)
                ).when(
                        new AddLoanToReaderCommand(loanId, readerId)
                )
                .expectResult(
                        true
                ).expectNoEvents();
    }

    @Test
    public void willRejectTooManyLoans(@Autowired CommandHandlingTestFixture<Reader, AddLoanToReaderCommand, Boolean> fixture) {

        var readerId = UUID.randomUUID();
        var loanId1 = UUID.randomUUID();
        var loanId2 = UUID.randomUUID();
        var loanId3 = UUID.randomUUID();

        fixture
                .given(
                        new ReaderRegisteredEvent(readerId, "Max", "Mustermann"),
                        new LoanAddedToReaderEvent(loanId1, readerId),
                        new LoanAddedToReaderEvent(loanId2, readerId)
                ).when(
                        new AddLoanToReaderCommand(loanId3, readerId)
                )
                .expectResult(
                        false
                ).expectNoEvents();
    }

}
