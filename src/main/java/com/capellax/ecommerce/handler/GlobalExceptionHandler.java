package com.capellax.ecommerce.handler;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
            MethodArgumentNotValidException exp
    ) {
        Map<String, String> errors = new HashMap<>();
        exp.getBindingResult()
                .getAllErrors()
                .forEach((error) -> {
                    String fieldName = ((FieldError) error).getField();
                    String fieldMessage = error.getDefaultMessage();
                    errors.put(fieldName, fieldMessage);
                });
        return new ResponseEntity<>(errors, BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGlobalExceptions(
            Exception exp
    ) {
        Map<String, String> error = new HashMap<>();
        error.put("error", exp.getMessage());
        return new ResponseEntity<>(error, INTERNAL_SERVER_ERROR);
    }

}
