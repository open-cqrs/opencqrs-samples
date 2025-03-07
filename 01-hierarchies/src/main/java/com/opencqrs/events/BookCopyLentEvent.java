package com.opencqrs.events;

import java.time.Instant;
import java.util.UUID;

public record BookCopyLentEvent(
        UUID id,
        String isbn,
        Instant returnDueAt
) {}