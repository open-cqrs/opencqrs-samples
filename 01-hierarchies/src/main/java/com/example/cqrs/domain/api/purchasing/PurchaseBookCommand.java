package com.example.cqrs.domain.api.purchasing;

import de.dxfrontiers.cqrs.framework.command.Command;
import java.util.UUID;

public record PurchaseBookCommand(
        String isbn,
        String title,
        String author,
        Long numPages
) implements Command {

    @Override
    public String getSubject() {
        return "/books/" + isbn();
    }
}