package com.example.cqrs.domain.LoanApplication.commands;

import java.util.UUID;

public record ApplyLoanRequestCommand(
        String applicationId,
        String applicant,
        String amount,
        String currency,
        String locationType
) implements LoanApplicationCommand {

    public ApplyLoanRequestCommand(String applicant, String amount, String currency, String locationType) {
        this(UUID.randomUUID().toString(), applicant, amount, currency, locationType);
    }

    @Override
    public String getApplicationId() {
        return applicationId;
    }

    @Override
    public SubjectCondition getSubjectCondition() {
        return SubjectCondition.PRISTINE;
    }
}
