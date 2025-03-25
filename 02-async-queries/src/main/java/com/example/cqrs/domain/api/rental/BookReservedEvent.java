package com.example.cqrs.domain.api.rental;

import java.time.Instant;

public record BookReservedEvent(
        String isbn,
        Instant dueAt
) {
}
