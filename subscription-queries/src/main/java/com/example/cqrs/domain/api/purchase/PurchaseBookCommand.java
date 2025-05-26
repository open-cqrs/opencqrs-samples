package com.example.cqrs.domain.api.purchase;

import com.opencqrs.framework.command.Command;

public record PurchaseBookCommand(
        String isbn,
        String title,
        String author
) implements Command {

    @Override
    public String getSubject() {
        return "/books/" + isbn();
    }
}
