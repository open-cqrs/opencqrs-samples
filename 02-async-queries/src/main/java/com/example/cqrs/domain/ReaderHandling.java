package com.example.cqrs.domain;

import com.example.cqrs.domain.api.registration.ReaderRegisteredEvent;
import com.example.cqrs.domain.api.registration.RegisterReaderCommand;
import com.opencqrs.framework.command.CommandEventPublisher;
import com.opencqrs.framework.command.CommandHandlerConfiguration;
import com.opencqrs.framework.command.CommandHandling;
import com.opencqrs.framework.command.StateRebuilding;

import java.util.UUID;

@CommandHandlerConfiguration
public class ReaderHandling {

    @CommandHandling
    public void handle(Reader reader, RegisterReaderCommand command, CommandEventPublisher<Reader> publisher) {
        if (reader == null) {
            publisher.publish(
                    new ReaderRegisteredEvent(command.id(), command.firstName(), command.lastName())
            );
        } else {
            throw new IllegalStateException("Reader already registered!");
        }
    }

    @StateRebuilding
    public Reader on(ReaderRegisteredEvent event) {
        return new Reader(event.id());
    }

}
