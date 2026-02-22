package com.example.demo.service;

import com.example.demo.model.User;

public interface EmailService {
    
    /**
     * Send email verification link to user
     */
    void sendVerificationEmail(User user, String token);
    
    /**
     * Send password reset link to user
     */
    void sendPasswordResetEmail(User user, String token);
    
    /**
     * Send welcome email after successful verification
     */
    void sendWelcomeEmail(User user);
}
