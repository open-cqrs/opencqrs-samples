package com.example.cqrs.domain.api.exception;

public final class UsernameAlreadyTakenException extends UserAccountException {
    public UsernameAlreadyTakenException(String username) {
        super("Username '" + username + "' is already taken.");
    }
}
