package com.opencqrs.events;

import java.util.UUID;

public record BookCopyAddedEvent(
        UUID id
) {
}
