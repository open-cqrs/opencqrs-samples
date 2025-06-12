package com.example.cqrs.domain;


import com.example.cqrs.domain.api.borrowing.BookCopyLentEvent;

import java.time.Instant;
import java.util.UUID;

public record BookCopy(
        UUID id,
        Instant dueDate
) {

    public BookCopy(UUID id) {
        this(id, null);
    }

    public boolean isLent() {
        return dueDate() != null;
    }

    public BookCopy withDueDate(Instant dueDate) {
        return new BookCopy(
                id(),
                dueDate
        );
    }
}
