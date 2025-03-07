package com.opencqrs.domain;

import com.opencqrs.domain.api.purchasing.PurchaseBookCommand;
import com.opencqrs.domain.api.purchasing.BookCopyAddedEvent;
import com.opencqrs.domain.api.purchasing.BookInformationAddedEvent;
import de.dxfrontiers.cqrs.framework.command.CommandEventPublisher;
import de.dxfrontiers.cqrs.framework.command.CommandHandlerConfiguration;
import de.dxfrontiers.cqrs.framework.command.CommandHandling;
import de.dxfrontiers.cqrs.framework.command.StateRebuilding;

import java.util.Collections;

@CommandHandlerConfiguration
public class BookHandling {

    @CommandHandling
    public void handle(Book book, PurchaseBookCommand command, CommandEventPublisher<Void> publisher) {

        if (book == null) {
            publisher.publish(
                    new BookInformationAddedEvent(
                            command.isbn(),
                            command.title(),
                            command.author(),
                            command.numPages()
                    )
            );
        }

        publisher.publish(
                new BookCopyAddedEvent(
                        command.id()
                )
        );
    }

    @StateRebuilding
    public Book on(BookInformationAddedEvent event) {
        return new Book(event.isbn(), event.title(), event.author(), event.numPages(), Collections.emptySet());
    }

    @StateRebuilding
    public Book on(Book book, BookCopyAddedEvent event) {
        return book.withAddedCopy(event.id());
    }
}
