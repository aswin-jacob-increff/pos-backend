package org.example.model.data;

public class ApiError {
    private int status;
    private String message;
    private String path;
    private String errorType;

    public ApiError() {
        this.errorType = "ERROR";
    }

    public ApiError(int status, String message, String path) {
        this.status = status;
        this.message = message;
        this.path = path;
        this.errorType = "ERROR";
    }

    public ApiError(int status, String message, String path, String errorType) {
        this.status = status;
        this.message = message;
        this.path = path;
        this.errorType = errorType;
    }

    // Getters and Setters
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }
} 