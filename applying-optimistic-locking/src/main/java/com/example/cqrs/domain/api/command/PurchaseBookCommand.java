package com.example.cqrs.domain.api.command;

import com.opencqrs.framework.command.Command;
import java.util.List;

public record PurchaseBookCommand(String isbn, String title, List<String> authors) implements Command {
    @Override
    public String getSubject() {
        return "/books/" + isbn;
    }

    @Override
    public SubjectCondition getSubjectCondition() {
        return SubjectCondition.PRISTINE;
    }
}
