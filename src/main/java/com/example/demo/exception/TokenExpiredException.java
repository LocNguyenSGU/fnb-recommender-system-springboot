package com.example.demo.exception;

public class TokenExpiredException extends UnauthorizedException {
    
    public TokenExpiredException(String message) {
        super(message);
    }
}
