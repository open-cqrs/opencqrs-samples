package com.example.cqrs.domain.LoanApplication.events;

public record LoanApplicationAppliedEvent(
        String applicationId,
        String applicant,
        String amount,
        String currency,
        String locationType,
        boolean verifiedAddress
) {}
