package com.example.cqrs.domain;

import com.example.cqrs.domain.api.rental.*;
import com.opencqrs.framework.command.*;
import com.opencqrs.framework.eventhandler.EventHandling;
import org.springframework.beans.factory.annotation.Autowired;

@CommandHandlerConfiguration
public class LoanHandling {

    @CommandHandling
    public void handle(StartLoanCommand command, CommandEventPublisher<Loan> publisher) {
        publisher.publish(
                new LoanStartedEvent(
                        command.loanId(),
                        command.readerId(),
                        command.isbn()
                )
        );
    }

    @StateRebuilding
    public Loan on(LoanStartedEvent event) {
        return new Loan(event.loanId(), event.readerId(), event.isbn());
    }

    @EventHandling("loan")
    public void on(LoanStartedEvent event, @Autowired CommandRouter router) {
        boolean success = router.send(
                new IncrementLentBookCountCommand(event.loanId(), event.readerId())
        );
        if(!success) {
            router.send(
                    new CancellLoanCommand(event.loanId())
            );
        }
    }

    @CommandHandling
    public void handle(Loan loan, RequestBookCommand command, CommandEventPublisher<Loan> publisher) {
        publisher.publish(
                new BookRequestedEvent(loan.id(), loan.isbn())
        );
    }

    @EventHandling("loan")
    public void on(BookRequestedEvent event, @Autowired CommandRouter router) {
        router.send(
                new ReserveBookCommand(event.loanId(), event.isbn())
        );
    }

    @CommandHandling
    public void handle(CompleteLoanCommand command, CommandEventPublisher<Loan> publisher) {
        publisher.publish(new LoanCompletedEvent(command.id()));
    }

    @CommandHandling
    public void handle(CancellLoanCommand command, CommandEventPublisher<Loan> publisher) {
        publisher.publish(new LoanCancelledEvent(command.id()));
    }
}
