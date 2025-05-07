package com.example.cqrs.domain.api.rental;

import java.util.UUID;

public record LentBookCountDecrementedEvent(
        UUID loandId,
        UUID readerId) {
}
