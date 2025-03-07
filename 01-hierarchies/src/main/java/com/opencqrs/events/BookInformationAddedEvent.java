package com.opencqrs.events;

import java.util.UUID;

public record BookInformationAddedEvent(
        String isbn,
        String title,
        String author,
        Long numPages
) {
}