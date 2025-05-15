package com.example.cqrs.domain;

import java.time.Instant;
import java.util.UUID;

public record Book(
        String isbn,
        UUID activeLoan
) {

    public Book(String isbn) {
        this(isbn, null);
    }

    public Book lendOut(UUID loanId) {
        return new Book(isbn(), loanId);
    }

    public boolean isLent() {
        return activeLoan != null;
    }
}
