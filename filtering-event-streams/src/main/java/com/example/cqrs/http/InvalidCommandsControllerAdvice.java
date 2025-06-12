package com.example.cqrs.http;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class InvalidCommandsControllerAdvice {

    @ExceptionHandler({IllegalStateException.class})
    public ResponseEntity<String> handleIllegalStateExcpetion(IllegalStateException ex) {
        return new ResponseEntity<String>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

}
