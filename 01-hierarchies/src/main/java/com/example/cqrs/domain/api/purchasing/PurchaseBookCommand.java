package com.example.cqrs.domain.api.purchasing;

import de.dxfrontiers.cqrs.framework.command.Command;

public record PurchaseBookCommand(
        String isbn,
        String title,
        String author,
        int numPages
) implements Command {

    @Override
    public String getSubject() {
        return "/books/" + isbn();
    }
}