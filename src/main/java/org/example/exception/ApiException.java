package org.example.exception;

public class ApiException extends RuntimeException {
    public ApiException(String message) {
        super(message);
    }
}