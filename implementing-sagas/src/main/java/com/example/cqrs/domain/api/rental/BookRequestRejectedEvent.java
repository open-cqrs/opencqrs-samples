package com.example.cqrs.domain.api.rental;

import java.util.UUID;

public record BookRequestRejectedEvent(
        UUID loandId,
        UUID readerId
) {
}
