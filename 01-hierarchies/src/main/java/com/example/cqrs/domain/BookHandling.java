package com.example.cqrs.domain;

import com.example.cqrs.domain.api.purchasing.PurchaseBookCommand;
import com.example.cqrs.domain.api.purchasing.BookCopyAddedEvent;
import com.example.cqrs.domain.api.purchasing.BookInformationAddedEvent;
import de.dxfrontiers.cqrs.framework.command.CommandEventPublisher;
import de.dxfrontiers.cqrs.framework.command.CommandHandlerConfiguration;
import de.dxfrontiers.cqrs.framework.command.CommandHandling;
import de.dxfrontiers.cqrs.framework.command.StateRebuilding;

import java.util.Collections;
import java.util.UUID;

@CommandHandlerConfiguration
public class BookHandling {

    @CommandHandling
    public UUID handle(Book book, PurchaseBookCommand command, CommandEventPublisher<Book> publisher) {

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

        var id = UUID.randomUUID();

        publisher.publishRelative("copies/" + id, new BookCopyAddedEvent(id));

        return id;
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
