package com.example.cqrs.domain;

import com.example.cqrs.domain.api.rental.IncrementLentBookCountCommand;
import com.example.cqrs.domain.api.rental.LentBookCountIncrementedEvent;
import com.example.cqrs.domain.api.rental.LoanStartedEvent;
import com.example.cqrs.domain.api.rental.StartLoanCommand;
import com.opencqrs.framework.command.*;
import com.opencqrs.framework.eventhandler.EventHandling;
import org.springframework.beans.factory.annotation.Autowired;

@CommandHandlerConfiguration
public class LoanHandling {

    @CommandHandling
    public void handle(Loan loan, StartLoanCommand command, CommandEventPublisher<Loan> publisher) {
        if (loan == null) {
            publisher.publish(
                    new LoanStartedEvent(
                            command.loanId(),
                            command.readerId(),
                            command.isbn()
                    )
            );
        } else {
            throw new IllegalStateException("This lending process has already been started!");
        }
    }

    @StateRebuilding
    public Loan on(LoanStartedEvent event) {
        return new Loan(event.loanId(), event.readerId(), event.isbn());
    }

    @EventHandling
    public void on(LoanStartedEvent event, @Autowired CommandRouter router) {
        router.send(
                new IncrementLentBookCountCommand(event.loanId(), event.readerId())
        );
    }

    @EventHandling
    public void on(LentBookCountIncrementedEvent event, @Autowired CommandRouter router) {

    }
}
