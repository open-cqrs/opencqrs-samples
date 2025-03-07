package com.opencqrs.events;

import java.util.UUID;

public record BookCopyReturnedEvent(
        UUID id,
        String isbn
) {}