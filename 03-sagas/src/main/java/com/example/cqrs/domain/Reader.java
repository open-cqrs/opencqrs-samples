package com.example.cqrs.domain;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public record Reader(
        UUID id,
        Set<UUID> activeLoans
) {
    public Reader(UUID id) { this(id, new HashSet<>()); }

    public Reader addLoan(UUID loanId) {
        activeLoans().add(loanId);
        return new Reader(id(), activeLoans());
    }

    public Reader removeLoan(UUID loanId) {
        activeLoans().remove(loanId);
        return new Reader(id(), activeLoans());
    }
}
