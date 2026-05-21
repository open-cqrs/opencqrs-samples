package com.example.cqrs.http;

import com.example.cqrs.domain.api.command.SignUpCommand;
import com.example.cqrs.domain.api.exception.*;
import com.opencqrs.framework.command.CommandSubjectAlreadyExistsException;
import com.opencqrs.framework.command.CommandSubjectDoesNotExistException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(UserAccountException.class)
    public ProblemDetail userAccountException(UserAccountException e) {
        HttpStatus status = switch (e) {
            case SameEmailException ignored -> HttpStatus.BAD_REQUEST;
            case SignUpPendingException ignored -> HttpStatus.CONFLICT;
            case EmailChangeInProgressException ignored -> HttpStatus.CONFLICT;
            case AccountDisabledException ignored -> HttpStatus.CONFLICT;
            case UsernameAlreadyTakenException ignored -> HttpStatus.CONFLICT;
        };
        return ProblemDetail.forStatusAndDetail(status, e.getMessage());
    }

    @ExceptionHandler(CommandSubjectAlreadyExistsException.class)
    public ProblemDetail alreadyExists(CommandSubjectAlreadyExistsException e) {
        if (e.getCommand() instanceof SignUpCommand signUp) {
            return userAccountException(new UsernameAlreadyTakenException(signUp.username()));
        }
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(CommandSubjectDoesNotExistException.class)
    public ProblemDetail notFound(CommandSubjectDoesNotExistException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }
}
