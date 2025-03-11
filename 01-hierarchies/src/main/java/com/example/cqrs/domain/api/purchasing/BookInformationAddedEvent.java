package com.example.cqrs.domain.api.purchasing;

public record BookInformationAddedEvent(
        String isbn,
        String title,
        String author,
        int numPages
) {
}