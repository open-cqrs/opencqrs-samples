package com.example.cqrs.domain;


import com.example.cqrs.domain.api.registration.ReaderRegisteredEvent;
import com.example.cqrs.domain.api.rental.BookReceivedEvent;
import com.example.cqrs.domain.api.rental.BookReturnedEvent;
import com.example.cqrs.domain.persistence.ReaderEntity;
import com.example.cqrs.domain.persistence.ReaderRepository;
import com.example.cqrs.service.SynchronizerService;
import com.opencqrs.framework.eventhandler.EventHandling;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ReaderProjector {

    private final ReaderRepository repository;
    private final SynchronizerService syncer;

    public ReaderProjector(ReaderRepository repository, SynchronizerService syncer) {
        this.repository = repository;
        this.syncer = syncer;
    }

    @EventHandling("reader")
    @Transactional
    public void on(ReaderRegisteredEvent event) {
        var entity = new ReaderEntity();
        entity.setId(event.id());
        repository.save(entity);
    }

    @EventHandling("reader")
    @Transactional
    public void on(BookReceivedEvent event, Map<String, String> metadata) {
        var entity = repository.findById(event.id()).orElseThrow(() -> new IllegalArgumentException("No such reader registered"));

        entity.getLentBookISBNs().add(event.isbn());

        var savedEntity = repository.save(entity);

        syncer.putLatestResultFor(metadata.get("correlation-id"), savedEntity.getLentBookISBNs().toArray());
    }

    @EventHandling("reader")
    @Transactional
    public void on(BookReturnedEvent event, Map<String, String> metadata) {
        var entity = repository.findById(event.id()).orElseThrow(() -> new IllegalArgumentException("No such reader registered"));

        entity.getLentBookISBNs().remove(event.isbn());

        var savedEntity = repository.save(entity);

        syncer.putLatestResultFor(metadata.get("correlation-id"), savedEntity.getLentBookISBNs().toArray());
    }
}
