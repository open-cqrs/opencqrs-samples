package com.example.cqrs.domain.api.rental;

import com.opencqrs.framework.command.Command;

import java.util.UUID;

public record DecrementLentBooksCounterCommand(
        UUID loanId,
        UUID readerId
) implements Command {
    @Override
    public String getSubject() {
        return "/readers/" + readerId();
    }
}
