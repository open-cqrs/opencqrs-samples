package com.example.cqrs.domain.LoanApplication;

public interface ManualReviewService {
    String fetchReviewResult(String applicationId);
}
