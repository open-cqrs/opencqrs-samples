package com.example.cqrs.domain.api.command;

import com.opencqrs.framework.command.Command;

public record RejectSignUpCommand(String username, String email) implements Command {
    @Override
    public String getSubject() {
        return "/user-accounts/" + username.toLowerCase();
    }
    @Override
    public SubjectCondition getSubjectCondition() {
        return SubjectCondition.EXISTS;
    }
}
