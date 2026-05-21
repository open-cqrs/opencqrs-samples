package com.example.cqrs.domain.api.exception;

public final class SameEmailException extends UserAccountException {
    public SameEmailException() {
        super("New email is the same as the current one.");
    }
}
