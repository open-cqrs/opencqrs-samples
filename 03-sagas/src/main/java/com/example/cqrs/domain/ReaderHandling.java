package com.example.cqrs.domain;

import com.example.cqrs.domain.api.registration.ReaderRegisteredEvent;
import com.example.cqrs.domain.api.registration.RegisterReaderCommand;
import com.example.cqrs.domain.api.rental.IncrementLentBookCountCommand;
import com.example.cqrs.domain.api.rental.LentBookCountIncrementedEvent;
import com.example.cqrs.domain.api.rental.RequestBookCommand;
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
    public boolean handle(Reader reader, IncrementLentBookCountCommand command, CommandEventPublisher<Reader> publisher) {
        if (reader != null && reader.lentBooks() < 2) {
            publisher.publish(new LentBookCountIncrementedEvent(command.loanId(), command.readerId()));
            return true;
        } else {
            return false;
        }
    }

    @StateRebuilding
    public Reader on(Reader reader, LentBookCountIncrementedEvent event) {
        return reader.incrementLentBooks();
    }

    @EventHandling("loan")
    public void on(LentBookCountIncrementedEvent event, @Autowired CommandRouter router) {
        router.send(new RequestBookCommand(event.loanId()));
    }
}
