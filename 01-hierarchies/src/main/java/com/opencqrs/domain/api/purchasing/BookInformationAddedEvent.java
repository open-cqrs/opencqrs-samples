package com.opencqrs.domain.api.purchasing;

public record BookInformationAddedEvent(
        String isbn,
        String title,
        String author,
        Long numPages
) {
}