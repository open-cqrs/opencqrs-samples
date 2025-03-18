package com.example.cqrs.domain.api.rental;

import com.opencqrs.framework.command.Command;

import java.util.UUID;

public record ReturnBookCommand(
        UUID readerId,
        String bookISBN
) implements Command {

    @Override
    public String getSubject() {
        return "/books/" + bookISBN();
    }
}