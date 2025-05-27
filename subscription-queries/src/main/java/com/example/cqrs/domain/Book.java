package com.example.cqrs.domain;

import java.time.Instant;

public record Book(
        String isbn,
        Instant dueDate
) {

    public Book(String isbn) {
        this(isbn, null);
    }

    public boolean isLent() {
        return dueDate != null;
    }
}
