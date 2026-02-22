package com.example.demo.exception;

public class EmailNotVerifiedException extends UnauthorizedException {
    
    public EmailNotVerifiedException(String message) {
        super(message);
    }
}
