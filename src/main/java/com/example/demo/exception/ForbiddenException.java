package com.example.demo.exception;

public class ForbiddenException extends RuntimeException {
    
    public ForbiddenException(String message) {
        super(message);
    }
    
    public ForbiddenException() {
        super("Forbidden - You don't have permission to access this resource");
    }
}
