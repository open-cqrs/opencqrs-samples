package com.example.cqrs.domain.LoanApplication.events;

public record LoanApplicationEnrichedEvent(
        String applicationId,
        String manualReviewResult
) {}
