package com.example.cqrs.domain;


import com.example.cqrs.domain.api.borrowing.BookCopyLentEvent;

import java.time.Instant;
import java.util.UUID;

public record BookCopy(
        UUID id,
        Instant dueDate,
        boolean isLent
) {

    public BookCopy withDueDate(Instant dueDate) {
        return new BookCopy(
                id(),
                dueDate,
                isLent()
        );
    }

    public BookCopy withRentalStatus(boolean status) {
        return new BookCopy(
                id(),
                dueDate(),
                status
        );
    }
}
