package com.example.cqrs.domain;


import com.example.cqrs.domain.api.registration.ReaderRegisteredEvent;
import com.example.cqrs.domain.api.rental.BookReceivedEvent;
import com.example.cqrs.domain.api.rental.BookReturnedEvent;
import com.example.cqrs.domain.persistence.ReaderEntity;
import com.example.cqrs.domain.persistence.ReaderRepository;
import com.opencqrs.framework.eventhandler.EventHandling;
import jakarta.transaction.Transactional;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ReaderProjector {

    private final ReaderRepository repository;
    private final MessageChannel channel;

    public ReaderProjector(ReaderRepository repository, MessageChannel channel) {
        this.repository = repository;
        this.channel = channel;
    }

    @EventHandling("reader")
    @Transactional
    public void on(ReaderRegisteredEvent event, Map<String, String> metadata) {
        var entity = new ReaderEntity();
        entity.setId(event.id());
        repository.save(entity);
        Message<String> message = MessageBuilder.withPayload(metadata.getOrDefault("correlation-id", "")).build();
        channel.send(message);
    }

    @EventHandling("reader")
    @Transactional
    public void on(BookReceivedEvent event, Map<String, String> metadata) {
        var entity = repository.findById(event.id()).orElseThrow(() -> new IllegalArgumentException("No such reader registered"));

        entity.getLentBookISBNs().add(event.isbn());

        repository.save(entity);

        Message<String> message = MessageBuilder.withPayload(metadata.getOrDefault("correlation-id", "")).build();
        channel.send(message);
    }

    @EventHandling("reader")
    @Transactional
    public void on(BookReturnedEvent event, Map<String, String> metadata) {
        var entity = repository.findById(event.id()).orElseThrow(() -> new IllegalArgumentException("No such reader registered"));

        entity.getLentBookISBNs().remove(event.isbn());

        repository.save(entity);

        Message<String> message = MessageBuilder.withPayload(metadata.getOrDefault("correlation-id", "")).build();
        channel.send(message);
    }
}
