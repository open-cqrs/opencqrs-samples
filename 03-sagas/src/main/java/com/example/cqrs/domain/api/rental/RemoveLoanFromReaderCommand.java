package com.example.cqrs.domain.api.rental;

import com.opencqrs.framework.command.Command;

import java.util.UUID;

public record RemoveLoanFromReaderCommand(
        UUID loanId,
        UUID readerId
) implements Command {
    @Override
    public String getSubject() {
        return "/readers/" + readerId();
    }

    @Override
    public SubjectCondition getSubjectCondition() { return SubjectCondition.EXISTS; }
}
