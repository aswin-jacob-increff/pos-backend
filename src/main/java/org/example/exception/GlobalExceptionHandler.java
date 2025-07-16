package org.example.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    static class ErrorResponse {
        public String message;
        public String errorType;
        public ErrorResponse(String message) { 
            this.message = message; 
            this.errorType = "ERROR";
        }
        public ErrorResponse(String message, String errorType) { 
            this.message = message; 
            this.errorType = errorType;
        }
    }

    // Existing exception handlers
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("Duplicate entry detected: " + extractConstraintMessage(ex)));
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("An unexpected error occurred: " + ex.getMessage()));
    }

    private String extractConstraintMessage(DataIntegrityViolationException ex) {
        String message = ex.getMessage();
        if (message != null && message.contains("Duplicate entry")) {
            return "Duplicate entry detected";
        }
        return "Data integrity violation";
    }
}
