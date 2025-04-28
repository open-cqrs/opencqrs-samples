package com.example.cqrs.domain.api.registration;

import java.util.UUID;

public record ReaderRegisteredEvent(
        UUID id,
        String firstName,
        String lastName
) {
}
