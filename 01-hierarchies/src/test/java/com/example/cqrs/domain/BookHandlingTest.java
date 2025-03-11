package com.example.cqrs.domain;

import com.example.cqrs.domain.api.purchasing.BookCopyAddedEvent;
import com.example.cqrs.domain.api.purchasing.BookInformationAddedEvent;
import com.example.cqrs.domain.api.purchasing.PurchaseBookCommand;
import de.dxfrontiers.cqrs.framework.command.CommandHandlingTest;
import de.dxfrontiers.cqrs.framework.command.CommandHandlingTestFixture;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;

@CommandHandlingTest
public class BookHandlingTest {

    @Test
    public void willAddBookInformation(@Autowired CommandHandlingTestFixture<Book, PurchaseBookCommand, UUID> fixture) {
        fixture
                .givenNothing()
                .when(new PurchaseBookCommand("012-34567890", "JRR Tolkien", "LOTR", 435))
                .expectSuccessfulExecution()
                .expectEvents(
                        new BookInformationAddedEvent("012-34567890", "JRR Tolkien", "LOTR", 435),
                        new BookCopyAddedEvent(any(UUID.class))
                );
    }
}
