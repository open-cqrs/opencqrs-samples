package com.example.cqrs.domain.api.exception;

public sealed abstract class UserAccountException extends RuntimeException
        permits SameEmailException,
                SignUpPendingException,
                EmailChangeInProgressException,
                AccountDisabledException,
                UsernameAlreadyTakenException {

    protected UserAccountException(String reason) {
        super(reason);
    }
}
