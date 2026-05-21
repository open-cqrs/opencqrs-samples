package com.example.cqrs.domain.api.command;

import java.util.List;

public record EditBookDetailsCommand(String isbn, String title, List<String> authors, String expectedVersion)
        implements VersionedCommand {

    @Override
    public String getSubject() {
        return "/books/" + isbn;
    }

    @Override
    public SubjectCondition getSubjectCondition() {
        return SubjectCondition.EXISTS;
    }
}
