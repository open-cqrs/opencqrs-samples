package com.example.cqrs.domain.api.rental;

import com.opencqrs.framework.command.Command;

public record CheckBookAvailabilityCommand(
        String isbn
) implements Command {
    @Override
    public String getSubject() {
        return "/books/" + isbn();
    }
}
