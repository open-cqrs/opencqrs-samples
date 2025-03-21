package com.example.cqrs.domain;


import com.example.cqrs.domain.api.registration.ReaderRegisteredEvent;
import com.example.cqrs.domain.api.rental.BookLentEvent;
import com.example.cqrs.domain.api.rental.BookReturnedEvent;
import com.example.cqrs.domain.persistence.ReaderEntity;
import com.example.cqrs.domain.persistence.ReaderRepository;
import com.opencqrs.framework.eventhandler.EventHandling;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReaderProjector {

    private final ReaderRepository repository;

    public ReaderProjector(ReaderRepository repository) {
        this.repository = repository;
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
    public void on(BookLentEvent event) {
        var entity = repository.findById(event.id()).orElseThrow(() -> new IllegalArgumentException("No such reader registered"));

        entity.getLentBookISBNs().add(event.isbn());

        repository.save(entity);
    }

    @EventHandling("reader")
    @Transactional
    public void on(BookReturnedEvent event) {
        var entity = repository.findById(event.id()).orElseThrow(() -> new IllegalArgumentException("No such reader registered"));

        entity.getLentBookISBNs().remove(event.isbn());

        repository.save(entity);
    }
}
