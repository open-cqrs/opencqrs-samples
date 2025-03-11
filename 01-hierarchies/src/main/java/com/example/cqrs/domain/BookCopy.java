package com.example.cqrs.domain;


import com.example.cqrs.domain.api.borrowing.BookCopyLentEvent;

import java.time.Instant;
import java.util.UUID;

public record BookCopy(
        UUID id,
        Instant dueDate
) {

    public boolean isLent() {
        return dueDate() != null;
    }

    public BookCopy withDueDate(Instant dueDate) {
        return new BookCopy(
                id(),
                dueDate
        );
    }

    public BookCopy withRentalStatus(boolean status) {
        return new BookCopy(
                id(),
                dueDate()
        );
    }
}
