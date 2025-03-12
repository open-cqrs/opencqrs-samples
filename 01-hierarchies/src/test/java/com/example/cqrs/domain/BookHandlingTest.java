package com.example.cqrs.domain;

import com.example.cqrs.domain.api.purchasing.BookCopyAddedEvent;
import com.example.cqrs.domain.api.purchasing.BookInformationAddedEvent;
import com.example.cqrs.domain.api.purchasing.PurchaseBookCommand;
import com.example.cqrs.services.UUIDGenerator;
import de.dxfrontiers.cqrs.framework.command.CommandHandlingTest;
import de.dxfrontiers.cqrs.framework.command.CommandHandlingTestFixture;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.mockito.Mockito.doReturn;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.UUID;

@CommandHandlingTest
public class BookHandlingTest {

    @MockitoBean
    private UUIDGenerator uuidGen;

    @Test
    public void willAddBookInformation(@Autowired CommandHandlingTestFixture<Book, PurchaseBookCommand, UUID> fixture) {

        var id = UUID.randomUUID();

        doReturn(id)
                .when(uuidGen)
                .getNextUUID();

        fixture
                .givenNothing()
                .when(new PurchaseBookCommand("012-34567890", "JRR Tolkien", "LOTR", 435))
                .expectSuccessfulExecution()
                .expectEvents(
                        new BookInformationAddedEvent("012-34567890", "JRR Tolkien", "LOTR", 435),
                        new BookCopyAddedEvent(id)
                )
                .expectState(new Book("012-34567890", "JRR Tolkien", "LOTR", 435, Collections.singleton(id)));
    }

    @Test
    public void willNotAddBookInformationWhenItAlreadyExists(@Autowired CommandHandlingTestFixture<Book, PurchaseBookCommand, UUID> fixture) {

        var initialId = UUID.randomUUID();
        var addedId = UUID.randomUUID();

        doReturn(addedId)
                .when(uuidGen)
                .getNextUUID();

        fixture
                .givenState(
                        new Book("012-34567890", "JRR Tolkien", "LOTR", 435, Collections.singleton(initialId))
                )
                .when(new PurchaseBookCommand("012-34567890", "JRR Tolkien", "LOTR", 435))
                .expectSingleEvent(new BookCopyAddedEvent(addedId))
                .expectState(
                        new Book("012-34567890", "JRR Tolkien", "LOTR", 435, new HashSet<>(Arrays.asList(initialId, addedId)))
                );
    }

    @Test
    public void willRejectPurchaseIfTooManyCopies(@Autowired CommandHandlingTestFixture<Book, PurchaseBookCommand, UUID> fixture) {

        var id1 = UUID.randomUUID();
        var id2 = UUID.randomUUID();
        var id3 = UUID.randomUUID();

        fixture
                .givenState(
                        new Book("012-34567890", "JRR Tolkien", "LOTR", 435, new HashSet<>(Arrays.asList(id1, id2, id3)))
                )
                .when(new PurchaseBookCommand("012-34567890", "JRR Tolkien", "LOTR", 435))
                .expectUnsuccessfulExecution();
    }

}
