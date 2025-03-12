package com.example.cqrs.domain;

import com.example.cqrs.domain.api.purchasing.PurchaseBookCommand;
import com.example.cqrs.domain.api.purchasing.BookCopyAddedEvent;
import com.example.cqrs.domain.api.purchasing.BookInformationAddedEvent;
import com.example.cqrs.services.UUIDGenerator;
import de.dxfrontiers.cqrs.framework.command.CommandEventPublisher;
import de.dxfrontiers.cqrs.framework.command.CommandHandlerConfiguration;
import de.dxfrontiers.cqrs.framework.command.CommandHandling;
import de.dxfrontiers.cqrs.framework.command.StateRebuilding;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.UUID;

@CommandHandlerConfiguration
public class BookHandling {

    @CommandHandling
    public UUID handle(Book book, PurchaseBookCommand command, CommandEventPublisher<Book> publisher, @Autowired UUIDGenerator uuidGen) {

        if (book == null) {
            publisher.publish(
                    new BookInformationAddedEvent(
                            command.isbn(),
                            command.title(),
                            command.author(),
                            command.numPages()
                    )
            );
        } else {
            if (book.copies().size() >= 3) {
                throw new IllegalStateException("Maximum number of copies per book reached (3)");
            }
        }

        var id = uuidGen.getNextUUID();

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
