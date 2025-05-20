package com.example.cqrs.domain.api.rental;

import com.opencqrs.framework.command.Command;

import java.util.UUID;

public record CancellLoanCommand(
        UUID id
) implements Command {
    @Override
    public String getSubject() {
        return "/loans/" + id();
    }
}
