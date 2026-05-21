package com.example.cqrs.domain;

import com.example.cqrs.domain.api.command.EditBookDetailsCommand;
import com.example.cqrs.domain.api.command.PurchaseBookCommand;
import com.example.cqrs.domain.api.event.BookDetailsEditedEvent;
import com.example.cqrs.domain.api.event.BookPurchasedEvent;
import com.opencqrs.framework.command.CommandHandlingTest;
import com.opencqrs.framework.command.CommandHandlingTestFixture;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ConcurrentModificationException;
import java.util.List;

@CommandHandlingTest
public class BookHandlingTest {

    private static final String ISBN = "978-0-261-10295-4";
    private static final String TITLE = "The Lord of the Rings";
    private static final List<String> AUTHORS = List.of("J.R.R. Tolkien");

    @Test
    public void purchaseBookPublishesPurchasedEvent(
            @Autowired CommandHandlingTestFixture<PurchaseBookCommand> fixture) {
        fixture
                .given()
                .nothing()
                .when(new PurchaseBookCommand(ISBN, TITLE, AUTHORS))
                .succeeds()
                .allEvents()
                .exactly(new BookPurchasedEvent(ISBN, TITLE, AUTHORS));
    }

    @Test
    public void editBookDetailsPublishesEditWhenTitleChanges(
            @Autowired CommandHandlingTestFixture<EditBookDetailsCommand> fixture) {
        fixture
                .given()
                .event(event -> event
                        .id("event-1")
                        .payload(new BookPurchasedEvent(ISBN, TITLE, AUTHORS)))
                .when(new EditBookDetailsCommand(
                        ISBN,
                        "The Fellowship of the Ring",
                        AUTHORS,
                        "event-1"))
                .succeeds()
                .allEvents()
                .exactly(new BookDetailsEditedEvent(
                        ISBN,
                        "The Fellowship of the Ring",
                        AUTHORS));
    }

    @Test
    public void editBookDetailsPublishesEditWhenAuthorsChange(
            @Autowired CommandHandlingTestFixture<EditBookDetailsCommand> fixture) {
        var edited = List.of("J.R.R. Tolkien", "Christopher Tolkien");
        fixture
                .given()
                .event(event -> event
                        .id("event-1")
                        .payload(new BookPurchasedEvent(ISBN, TITLE, AUTHORS)))
                .when(new EditBookDetailsCommand(ISBN, TITLE, edited, "event-1"))
                .succeeds()
                .allEvents()
                .exactly(new BookDetailsEditedEvent(ISBN, TITLE, edited));
    }

    @Test
    public void editBookDetailsIsNoOpWhenNothingChanges(
            @Autowired CommandHandlingTestFixture<EditBookDetailsCommand> fixture) {
        fixture
                .given()
                .event(event -> event
                        .id("event-1")
                        .payload(new BookPurchasedEvent(ISBN, TITLE, AUTHORS)))
                .when(new EditBookDetailsCommand(ISBN, TITLE, AUTHORS, "event-1"))
                .succeeds()
                .withoutEvents();
    }

    @Test
    public void editBookDetailsRejectsStaleVersion(
            @Autowired CommandHandlingTestFixture<EditBookDetailsCommand> fixture) {
        fixture
                .given()
                .event(event -> event
                        .id("event-2")
                        .payload(new BookPurchasedEvent(ISBN, TITLE, AUTHORS)))
                .when(new EditBookDetailsCommand(ISBN, "New Title", AUTHORS, "event-1"))
                .fails()
                .throwing(ConcurrentModificationException.class);
    }
}
