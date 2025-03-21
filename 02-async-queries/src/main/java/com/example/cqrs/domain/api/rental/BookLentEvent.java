package com.example.cqrs.domain.api.rental;

import java.time.Instant;
import java.util.UUID;

public record BookLentEvent(
        UUID id,
        String isbn,
        Instant dueAt
)  {
}
