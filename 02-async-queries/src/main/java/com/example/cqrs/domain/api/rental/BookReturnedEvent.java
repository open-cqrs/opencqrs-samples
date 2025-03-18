package com.example.cqrs.domain.api.rental;

import java.util.UUID;

public record BookReturnedEvent(
        UUID readerId,
        String bookISBN
)  {
}
