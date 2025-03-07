package com.example.cqrs.domain.api.borrowing;

import java.time.Instant;
import java.util.UUID;

public record BookCopyLentEvent(
        UUID id,
        String isbn,
        Instant returnDueAt
) {}