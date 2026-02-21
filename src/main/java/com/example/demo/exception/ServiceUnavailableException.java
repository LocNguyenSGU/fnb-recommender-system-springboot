package com.example.demo.exception;

public class ServiceUnavailableException extends RuntimeException {
    
    public ServiceUnavailableException(String message) {
        super(message);
    }
    
    public ServiceUnavailableException(String service, String reason) {
        super(String.format("%s service is unavailable: %s", service, reason));
    }
}
