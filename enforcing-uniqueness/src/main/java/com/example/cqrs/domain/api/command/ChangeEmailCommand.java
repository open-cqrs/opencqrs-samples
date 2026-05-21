package com.example.cqrs.domain.api.command;

import com.opencqrs.framework.command.Command;

public record ChangeEmailCommand(String username, String newEmail) implements Command {
    @Override
    public String getSubject() {
        return "/user-accounts/" + username.toLowerCase();
    }
    @Override
    public SubjectCondition getSubjectCondition() {
        return SubjectCondition.EXISTS;
    }
}
