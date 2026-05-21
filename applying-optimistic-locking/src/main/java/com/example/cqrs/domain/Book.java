package com.example.cqrs.domain;

import com.example.cqrs.domain.api.Versioned;
import com.opencqrs.esdb.client.Event;

import java.util.List;

public record Book(String version, String isbn, String title, List<String> authors) implements Versioned {
    public Book(Event event, String isbn, String title, List<String> authors) {
        this(event != null ? event.id() : null, isbn, title, authors);
    }
}
