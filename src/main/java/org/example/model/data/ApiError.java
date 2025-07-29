package org.example.model.data;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ApiError {
    // Getters and Setters
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

}