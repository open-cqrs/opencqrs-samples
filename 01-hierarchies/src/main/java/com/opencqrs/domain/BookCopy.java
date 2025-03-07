package com.opencqrs.domain;


import java.time.Instant;
import java.util.UUID;

public record BookCopy(
        UUID id,
        Instant dueDate,
        boolean isLent
) {

    public BookCopy withRentalStatus(boolean status) {
        return new BookCopy(
                id(),
                dueDate(),
                status
        );
    }
}
