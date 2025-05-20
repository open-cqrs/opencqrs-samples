package com.example.cqrs.domain.api.purchasing;

import java.util.UUID;

public record BookCopyAddedEvent(
        UUID id
) {
}
