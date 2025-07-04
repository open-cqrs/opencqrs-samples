package com.example.cqrs.domain.api.registration;

import com.opencqrs.framework.command.Command;

import java.util.UUID;

public record RegisterReaderCommand(
        UUID id,
        String firstName,
        String lastName
) implements Command {

    @Override
    public String getSubject() {
        return "/readers/" + id();
    }

    @Override
    public SubjectCondition getSubjectCondition() {
        return SubjectCondition.PRISTINE;
    }
}
