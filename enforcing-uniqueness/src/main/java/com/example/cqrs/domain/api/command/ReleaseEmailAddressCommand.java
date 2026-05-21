package com.example.cqrs.domain.api.command;

import com.opencqrs.framework.command.Command;

public record ReleaseEmailAddressCommand(String email, String username) implements Command {
    @Override
    public String getSubject() {
        return "/email-addresses/" + ReserveEmailAddressCommand.sha256(email);
    }

    @Override
    public SubjectCondition getSubjectCondition() {
        return SubjectCondition.EXISTS;
    }
}
