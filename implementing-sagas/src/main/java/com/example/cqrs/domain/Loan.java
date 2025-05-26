package com.example.cqrs.domain;

import java.util.UUID;

public record Loan(
        UUID id,
        UUID readerId,
        String isbn
) {
}
