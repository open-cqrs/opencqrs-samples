package com.example.cqrs.http;

import com.opencqrs.framework.client.ConcurrencyException;
import com.opencqrs.framework.command.CommandSubjectAlreadyExistsException;
import com.opencqrs.framework.command.CommandSubjectDoesNotExistException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ConcurrentModificationException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ConcurrentModificationException.class)
    public ProblemDetail handleVersionConflict(ConcurrentModificationException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.PRECONDITION_FAILED, e.getMessage());
        problem.setTitle("Version conflict");
        return problem;
    }

    @ExceptionHandler(ConcurrencyException.class)
    public ProblemDetail handleConcurrency(ConcurrencyException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.PRECONDITION_FAILED, e.getMessage());
        problem.setTitle("Concurrent write conflict");
        return problem;
    }

    @ExceptionHandler(CommandSubjectAlreadyExistsException.class)
    public ProblemDetail handleAlreadyExists(CommandSubjectAlreadyExistsException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(CommandSubjectDoesNotExistException.class)
    public ProblemDetail handleNotFound(CommandSubjectDoesNotExistException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }
}
