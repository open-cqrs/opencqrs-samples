package com.example.cqrs.domain.api.command;

import com.opencqrs.framework.command.Command;

public record CompleteEmailChangeCommand(String username) implements Command {
    @Override
    public String getSubject() {
        return "/user-accounts/" + username.toLowerCase();
    }
    @Override
    public SubjectCondition getSubjectCondition() {
        return SubjectCondition.EXISTS;
    }
}
