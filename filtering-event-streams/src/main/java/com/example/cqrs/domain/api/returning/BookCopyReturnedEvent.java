package com.example.cqrs.domain.api.returning;

import java.util.UUID;

/**
 *
 * @see ReturnBookCommand
 *
 * @param id UUID of the returned book copy
 * @param isbn ISBN of the book, whose copy was returned
 */
public record BookCopyReturnedEvent(
        UUID id,
        String isbn
) {}