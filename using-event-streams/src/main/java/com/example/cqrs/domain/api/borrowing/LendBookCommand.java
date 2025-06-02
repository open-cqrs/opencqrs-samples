package com.example.cqrs.domain.api.borrowing;

import com.opencqrs.framework.command.Command;

import java.util.UUID;

public record LendBookCommand(
        UUID id,
        String isbn
) implements Command {

    @Override
    public String getSubject() {
        return "/books/" + isbn() + "/copies/" + id();
    }

    @Override
    public SubjectCondition getSubjectCondition() { return SubjectCondition.EXISTS; }
}