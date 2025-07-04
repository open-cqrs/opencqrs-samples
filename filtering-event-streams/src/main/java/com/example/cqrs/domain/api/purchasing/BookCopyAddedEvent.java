package com.example.cqrs.domain.api.purchasing;

import java.util.UUID;

/**
 *
 * @see PurchaseBookCommand
 *
 * @param id UUID of the book copy that was added
 */
public record BookCopyAddedEvent(
        UUID id
) {
}
