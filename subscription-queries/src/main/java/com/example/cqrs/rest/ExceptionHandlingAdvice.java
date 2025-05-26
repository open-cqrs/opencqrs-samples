package com.example.cqrs.rest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class ExceptionHandlingAdvice {

    @ExceptionHandler({InterruptedException.class})
    @ResponseStatus(HttpStatus.GATEWAY_TIMEOUT)
    public void handleInterruption() { }

}
