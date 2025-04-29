package com.example.cqrs.domain.api.rental;

import java.util.UUID;

public record LoanStartedEvent(
        UUID loanId,
        UUID readerId,
        String isbn) {
}
