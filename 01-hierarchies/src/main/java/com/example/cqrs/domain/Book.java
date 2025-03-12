package com.example.cqrs.domain;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public record Book(
        String isbn,
        String title,
        String author,
        int numPages,
        Set<UUID> copies
) {
    public Book withAddedCopy(UUID newExemplarId) {
        var updatedExemplars = new HashSet<UUID>(copies);
        updatedExemplars.add(newExemplarId);

        return new Book(
                isbn(),
                title(),
                author(),
                numPages(),
                updatedExemplars
        );
    }
}