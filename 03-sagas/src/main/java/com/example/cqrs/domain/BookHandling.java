package com.example.cqrs.domain;

import com.example.cqrs.domain.api.purchase.BookPurchasedEvent;
import com.example.cqrs.domain.api.purchase.PurchaseBookCommand;
import com.opencqrs.framework.command.CommandEventPublisher;
import com.opencqrs.framework.command.CommandHandlerConfiguration;
import com.opencqrs.framework.command.CommandHandling;
import com.opencqrs.framework.command.StateRebuilding;

@CommandHandlerConfiguration
public class BookHandling {

    @CommandHandling
    public void handle(Book book, PurchaseBookCommand command, CommandEventPublisher<Book> publisher) {
        if (book == null) {
            publisher.publish(
                    new BookPurchasedEvent(
                            command.isbn(),
                            command.title(),
                            command.author()
                    )
            );
        } else {
            throw new IllegalStateException("Book already in stock");
        }
    }

    @StateRebuilding
    public Book on(BookPurchasedEvent event) {
        return new Book(event.isbn());
    }
}
