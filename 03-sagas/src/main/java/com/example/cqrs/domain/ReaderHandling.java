package com.example.cqrs.domain;

import com.example.cqrs.domain.api.registration.ReaderRegisteredEvent;
import com.example.cqrs.domain.api.registration.RegisterReaderCommand;
import com.example.cqrs.domain.api.rental.*;
import com.opencqrs.framework.command.*;
import com.opencqrs.framework.eventhandler.EventHandling;
import org.springframework.beans.factory.annotation.Autowired;

@CommandHandlerConfiguration
public class ReaderHandling {

    @CommandHandling
    public void handle(RegisterReaderCommand command, CommandEventPublisher<Reader> publisher) {
        publisher.publish(
                new ReaderRegisteredEvent(command.id(), command.firstName(), command.lastName())
        );
    }

    @StateRebuilding
    public Reader on(ReaderRegisteredEvent event) {
        return new Reader(event.id());
    }

    @CommandHandling
    public Boolean handle(Reader reader, AddLoanToReaderCommand command, CommandEventPublisher<Reader> publisher) {
        if (!reader.activeLoans().contains(command.loanId())) {
            if (reader.activeLoans().size() < 2) {
                publisher.publish(new LoanAddedToReaderEvent(command.loanId(), command.readerId()));
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    @StateRebuilding
    public Reader on(Reader reader, LoanAddedToReaderEvent event) {
        return reader.addLoan(event.loanId());
    }

    @EventHandling("loan")
    public void on(LoanAddedToReaderEvent event, @Autowired CommandRouter router) {
        router.send(new RequestBookCommand(event.loanId()));
    }

    @CommandHandling
    public void handle(RemoveLoanFromReaderCommand command, CommandEventPublisher<Reader> publisher) {
        publisher.publish(new LoanRemovedFromReaderEvent(command.loanId(), command.readerId()));
    }

    @StateRebuilding
    public Reader on(Reader reader, LoanRemovedFromReaderEvent event) {
        return reader.removeLoan(event.loandId());
    }

    @EventHandling("loan")
    public void on(LoanRemovedFromReaderEvent event, @Autowired CommandRouter router) {
        router.send(
                new CancellLoanCommand(event.loandId())
        );
    }
}
