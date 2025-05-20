package com.example.cqrs.domain.api.rental;

import java.util.UUID;

public record BookReservedEvent(
        UUID loanId,
        String isbn
) {
}
