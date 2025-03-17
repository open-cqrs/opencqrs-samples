package com.example.cqrs.domain;

import com.example.cqrs.domain.api.purchasing.BookCopyAddedEvent;
import com.example.cqrs.domain.api.purchasing.BookInformationAddedEvent;
import com.example.cqrs.domain.api.purchasing.PurchaseBookCommand;
import com.example.cqrs.utils.UUIDGenerator;
import de.dxfrontiers.cqrs.framework.command.CommandHandlingTest;
import de.dxfrontiers.cqrs.framework.command.CommandHandlingTestFixture;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.mockito.Mockito.doReturn;

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
                .expectNoMoreEvents();
    }

    @Test
    public void willNotAddBookInformationWhenItAlreadyExists(@Autowired CommandHandlingTestFixture<Book, PurchaseBookCommand, UUID> fixture) {

        var initialId = UUID.randomUUID();
        var addedId = UUID.randomUUID();

        doReturn(addedId)
                .when(uuidGen)
                .getNextUUID();

        fixture
                .given(
                        new BookInformationAddedEvent("012-34567890", "JRR Tolkien", "LOTR", 435),
                        new BookCopyAddedEvent(initialId)
                )
                .when(new PurchaseBookCommand("012-34567890", "JRR Tolkien", "LOTR", 435))
                .expectSuccessfulExecution()
                .expectSingleEvent(new BookCopyAddedEvent(addedId));
    }

    @Test
    public void willRejectPurchaseIfTooManyCopies(@Autowired CommandHandlingTestFixture<Book, PurchaseBookCommand, UUID> fixture) {

        var id1 = UUID.randomUUID();
        var id2 = UUID.randomUUID();
        var id3 = UUID.randomUUID();

        fixture
                .given(
                        new BookInformationAddedEvent("012-34567890", "JRR Tolkien", "LOTR", 435),
                        new BookCopyAddedEvent(id1),
                        new BookCopyAddedEvent(id2),
                        new BookCopyAddedEvent(id3)
                )
                .when(new PurchaseBookCommand("012-34567890", "JRR Tolkien", "LOTR", 435))
                .expectException(IllegalStateException.class);
    }

}
