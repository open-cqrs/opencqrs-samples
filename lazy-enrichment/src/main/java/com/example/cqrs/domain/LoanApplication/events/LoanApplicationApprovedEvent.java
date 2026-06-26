package com.example.cqrs.domain.LoanApplication.events;

public record LoanApplicationApprovedEvent(
        String applicationId
) {}
