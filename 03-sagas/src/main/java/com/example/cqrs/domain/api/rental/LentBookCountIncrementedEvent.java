package com.example.cqrs.domain.api.rental;

import java.util.UUID;

public record LentBookCountIncrementedEvent(
        UUID loanId,
        UUID readerId) {
}
