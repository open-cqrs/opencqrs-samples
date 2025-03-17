package com.example.cqrs.domain.api.returning;

import com.opencqrs.framework.command.Command;

import java.util.UUID;

public record ReturnBookCommand(
        UUID id,
        String isbn
) implements Command {

    @Override
    public String getSubject() {
        return "/books/" + isbn() + "/copies/" + id();
    }
}