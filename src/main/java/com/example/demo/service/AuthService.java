package com.example.demo.service;

import com.example.demo.dto.request.*;
import com.example.demo.dto.response.AuthResponse;
import com.example.demo.dto.response.MessageResponse;

public interface AuthService {
    
    MessageResponse register(RegisterRequest request);
    
    MessageResponse verifyEmail(String token);
    
    AuthResponse login(LoginRequest request);
    
    AuthResponse refreshToken(String refreshToken);
    
    MessageResponse logout(String refreshToken);
    
    MessageResponse forgotPassword(String email);
    
    MessageResponse resetPassword(String token, String newPassword);
    
    MessageResponse resendVerificationEmail(String email);
}
