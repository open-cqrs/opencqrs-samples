package com.example.cqrs.domain.LoanApplication.commands;

import com.opencqrs.framework.command.Command;

public sealed interface LoanApplicationCommand extends Command permits ApplyLoanRequestCommand, ApproveLoanCommand {

    String getApplicationId();

    @Override
    default String getSubject() {
        return "/loan-applications/" + getApplicationId();
    }
}
