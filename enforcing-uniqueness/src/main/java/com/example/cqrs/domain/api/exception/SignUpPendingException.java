package com.example.cqrs.domain.api.exception;

public final class SignUpPendingException extends UserAccountException {
    public SignUpPendingException() {
        super("Cannot change email: sign-up is still pending.");
    }
}
