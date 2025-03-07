package com.example.cqrs.domain.api.purchasing;

import de.dxfrontiers.cqrs.framework.command.Command;
import java.util.UUID;

public record PurchaseBookCommand(
        UUID id,
        String isbn,
        String title,
        String author,
        Long numPages
) implements Command {

    public PurchaseBookCommand(String isbn, String title, String author, Long numPages) {
        this(UUID.randomUUID(), isbn, title, author, numPages);
    }

    @Override
    public String getSubject() {
        return "/books/" + isbn() + "/exemplars/" + id();
    }
}