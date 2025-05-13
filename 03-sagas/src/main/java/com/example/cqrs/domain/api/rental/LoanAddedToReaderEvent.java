package com.example.cqrs.domain.api.rental;

import java.util.UUID;

public record LoanAddedToReaderEvent(
        UUID loanId,
        UUID readerId) {
}
