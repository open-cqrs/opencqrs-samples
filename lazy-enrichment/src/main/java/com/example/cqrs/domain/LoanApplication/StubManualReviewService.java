package com.example.cqrs.domain.LoanApplication;

import org.springframework.stereotype.Service;

@Service
public class StubManualReviewService implements ManualReviewService {

    // Stand-in for an outbound call to the compliance system (REST/gRPC/queue).
    // Returning a deterministic value keeps the sample reproducible.
    @Override
    public String fetchReviewResult(String applicationId) {
        return "COMPLIANT";
    }
}
