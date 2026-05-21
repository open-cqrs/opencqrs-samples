package com.example.cqrs.domain.api.exception;

public final class AccountDisabledException extends UserAccountException {
    public AccountDisabledException() {
        super("Cannot change email: account is NotRegistered.");
    }
}
