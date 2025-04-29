package com.example.cqrs.domain.api.rental;

import com.opencqrs.framework.command.Command;

import java.util.UUID;

public record StartLoanCommand(
        UUID loanId,
        UUID readerId,
        String isbn
) implements Command {

    @Override
    public String getSubject() {
        return "/loans/" + loanId();
    }
}
