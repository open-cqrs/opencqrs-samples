package com.example.cqrs.domain;

import com.example.cqrs.domain.api.event.BookDetailsEditedEvent;
import com.example.cqrs.domain.api.event.BookPurchasedEvent;
import com.opencqrs.esdb.client.Event;
import com.opencqrs.framework.eventhandler.EventHandling;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class BookCatalogProjection {

    private final ConcurrentMap<String, BookView> catalog = new ConcurrentHashMap<>();

    @EventHandling("book-catalog")
    public void on(BookPurchasedEvent event, Event rawEvent) {
        catalog.put(event.isbn(), new BookView(rawEvent.id(), event.isbn(), event.title(), List.copyOf(event.authors())));
    }

    @EventHandling("book-catalog")
    public void on(BookDetailsEditedEvent event, Event rawEvent) {
        catalog.computeIfPresent(event.isbn(), (isbn, current) ->
                new BookView(rawEvent.id(), isbn, event.title(), List.copyOf(event.authors())));
    }

    public Optional<BookView> findByIsbn(String isbn) {
        return Optional.ofNullable(catalog.get(isbn));
    }

    public Collection<BookView> findAll() {
        return catalog.values();
    }

    public record BookView(String version, String isbn, String title, List<String> authors) {
    }

}




