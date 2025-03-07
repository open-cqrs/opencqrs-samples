package com.opencqrs.domain.api.returning;

import de.dxfrontiers.cqrs.framework.command.Command;

import java.util.UUID;

public record ReturnBookCommand(
        UUID id,
        String isbn
) implements Command {

    @Override
    public String getSubject() {
        return "/books/" + isbn() + "/exemplars/" + id();
    }
}