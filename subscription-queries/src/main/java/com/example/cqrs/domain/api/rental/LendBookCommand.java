package com.example.cqrs.domain.api.rental;

import com.opencqrs.framework.command.Command;

import java.util.UUID;

public record LendBookCommand(
        UUID id,
        String isbn
) implements Command {

    @Override
    public String getSubject() {
        return "/books/" + isbn();
    }
}
