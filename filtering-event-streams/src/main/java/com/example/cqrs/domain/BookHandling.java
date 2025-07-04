package com.example.cqrs.domain;

import com.example.cqrs.domain.api.purchasing.PurchaseBookCommand;
import com.example.cqrs.domain.api.purchasing.BookCopyAddedEvent;
import com.example.cqrs.domain.api.purchasing.BookInformationAddedEvent;
import com.example.cqrs.utils.UUIDGenerator;
import com.opencqrs.framework.command.CommandEventPublisher;
import com.opencqrs.framework.command.CommandHandlerConfiguration;
import com.opencqrs.framework.command.CommandHandling;
import com.opencqrs.framework.command.StateRebuilding;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.UUID;

@CommandHandlerConfiguration
public class BookHandling {

    @CommandHandling
    public UUID handle(Book book, PurchaseBookCommand command, CommandEventPublisher<Book> publisher, @Autowired UUIDGenerator uuidGen) {

        /*
         * Note:
         *
         * For the purposes of this sample app, the book-purchasing logic has been simplified into a single command.
         *
         * The proper way of handling a purchase would have been to have a separate 'Purchase Book Copy' and 'Add Book Information' commands,
         * where the latter would be issued while handling the BookCopyAddedEvent fired by the former.
         *
         * Both commands should operate under the 'PRISTINE' subject-condition.
         */
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

        UUID id = uuidGen.getNextUUID();
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
