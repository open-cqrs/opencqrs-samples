package com.example.cqrs.domain;

import com.example.cqrs.domain.api.purchasing.BookCopyAddedEvent;
import com.example.cqrs.domain.api.purchasing.BookInformationAddedEvent;
import com.example.cqrs.domain.api.purchasing.PurchaseBookCommand;
import com.example.cqrs.services.UUIDGeneratorService;
import de.dxfrontiers.cqrs.framework.command.CommandHandlingTest;
import de.dxfrontiers.cqrs.framework.command.CommandHandlingTestFixture;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.mockito.Mockito.doReturn;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;

@CommandHandlingTest
public class BookHandlingTest {

    @MockitoBean
    private UUIDGeneratorService uuidGen;

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
                );
    }

    @Test
    public void willNotAddBookInformationWhenItAlreadyExists(@Autowired CommandHandlingTestFixture<Book, PurchaseBookCommand, UUID> fixture) {

        var id = UUID.randomUUID();

        doReturn(id)
                .when(uuidGen)
                .getNextUUID();

        fixture
                .givenState(
                        new Book("012-34567890", "JRR Tolkien", "LOTR", 435, Collections.singleton(UUID.randomUUID()))
                )
                .when(new PurchaseBookCommand("012-34567890", "JRR Tolkien", "LOTR", 435))
                .expectSingleEvent(new BookCopyAddedEvent(id));
    }

}
