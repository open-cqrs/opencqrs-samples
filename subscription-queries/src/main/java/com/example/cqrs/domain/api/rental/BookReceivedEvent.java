package com.example.cqrs.domain.api.rental;

import java.util.UUID;

public record BookReceivedEvent(
        UUID id,
        String isbn
)  {
}
