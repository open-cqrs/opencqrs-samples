package com.example.cqrs.domain.api.exception;

public final class EmailChangeInProgressException extends UserAccountException {
    public EmailChangeInProgressException() {
        super("Another email change is already in progress.");
    }
}
