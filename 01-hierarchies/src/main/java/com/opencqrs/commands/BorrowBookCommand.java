package com.opencqrs.commands;

import de.dxfrontiers.cqrs.framework.command.Command;

import java.util.UUID;

public record BorrowBookCommand(
        UUID id,
        String isbn
) implements Command {

    @Override
    public String getSubject() {
        return "/books/" + isbn() + "/exemplars/" + id();
    }
}