package com.example.cqrs.domain.api.rental;

import java.util.UUID;

public record BookLentEvent(
        UUID readerId,
        String bookISBN
)  {
}
