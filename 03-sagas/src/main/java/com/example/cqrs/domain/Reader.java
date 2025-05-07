package com.example.cqrs.domain;

import java.util.UUID;

public record Reader(
        UUID id,
        int lentBooks
) {
    public Reader(UUID id) { this(id, 0); }

    public Reader incrementLentBooks() {
        return new Reader(id(), lentBooks()+1);
    }

    public Reader decrementLentBooks() {
        return new Reader(id(), lentBooks()-1);
    }
}
