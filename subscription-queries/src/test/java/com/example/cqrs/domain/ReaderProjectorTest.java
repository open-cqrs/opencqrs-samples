package com.example.cqrs.domain;

import com.example.cqrs.domain.api.registration.ReaderRegisteredEvent;
import com.example.cqrs.domain.api.rental.BookReceivedEvent;
import com.example.cqrs.domain.api.rental.BookReturnedEvent;
import com.example.cqrs.domain.persistence.ReaderEntity;
import com.example.cqrs.domain.persistence.ReaderRepository;
import com.example.cqrs.async.CommandBridge;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(ReaderProjector.class)
public class ReaderProjectorTest {

    @Autowired private ReaderRepository repository;
    @MockitoBean private CommandBridge syncer;
    @MockitoBean private MessageChannel channel;

    @Autowired private ReaderProjector projector;

    private UUID id = UUID.fromString("7aeb55fe-7fea-461c-b11f-db7fc8ec775b");
    private String isbn1 = "978-0008471286";
    private String isbn2 = "978-0747532743";
    private Map<String, String> metadata = Map.of("correlation-id", "cid");


    @BeforeEach
    public void setup() {
        when(channel.send(any(Message.class))).thenReturn(true);
    }

    @Test
    public void registerReader() {
        projector.on(new ReaderRegisteredEvent(id, "Max", "Mustermann"), metadata);

        var entity = repository.findById(id);

        assert(entity.isPresent());
    }

    @Test
    public void receiveBooks() {

        var entity = new ReaderEntity();
        entity.setId(id);

        repository.save(entity);

        projector.on(new BookReceivedEvent(id, isbn1), metadata);
        projector.on(new BookReceivedEvent(id, isbn2), metadata);

        repository.findById(id).ifPresentOrElse(
                updated -> {
                    assert(updated.getLentBookISBNs().equals(new HashSet<>(Arrays.asList(isbn1, isbn2))));
                },
                () -> fail("No reader entity in DB")
        );
    }

    @Test
    public void returnBooks() {

        var entity = new ReaderEntity();
        entity.setId(id);
        entity.getLentBookISBNs().addAll(Arrays.asList(isbn1, isbn2));

        repository.save(entity);

        projector.on(new BookReturnedEvent(id, isbn1), metadata);
        projector.on(new BookReturnedEvent(id, isbn2), metadata);

        repository.findById(id).ifPresentOrElse(
                updated -> {
                    assert(updated.getLentBookISBNs().isEmpty());
                },
                () -> fail("No reader entity in DB")
        );
    }
}
