package com.opencqrs.domain.api.returning;

import java.util.UUID;

public record BookCopyReturnedEvent(
        UUID id,
        String isbn
) {}