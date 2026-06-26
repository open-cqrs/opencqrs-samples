package com.example.cqrs.domain.LoanApplication;

public record LoanRequest(
        String applicationId,
        String applicant,
        String amount,
        String currency,
        String locationType,
        boolean verifiedAddress,
        String manualReviewResult
) {
}
