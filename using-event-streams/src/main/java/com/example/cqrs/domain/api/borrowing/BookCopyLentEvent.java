package com.example.cqrs.domain.api.borrowing;

import java.time.Instant;
import java.util.UUID;

/**
 *
 * @see LendBookCommand
 *
 * @param id UUID of the lent book copy
 * @param isbn ISBN of the book, whose copy was lent
 * @param returnDueAt Return date of the lent book copy
 */
public record BookCopyLentEvent(
        UUID id,
        String isbn,
        Instant returnDueAt
) {}