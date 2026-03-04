package com.example.demo.service.impl;

import com.example.demo.model.User;
import com.example.demo.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${app.frontend-url}")
    private String frontendUrl;
    
    @Value("${app.token.verification-expiry}")
    private Long verificationTokenExpiry;
    
    @Value("${app.token.reset-expiry}")
    private Long resetTokenExpiry;
    
    @Override
    @Async
    public void sendVerificationEmail(User user, String token) {
        try {
            String verificationLink = frontendUrl + "/verify-email?token=" + token;
            long expiryHours = verificationTokenExpiry / (1000 * 60 * 60);
            
            Context context = new Context();
            context.setVariable("userName", user.getFullName());
            context.setVariable("verificationLink", verificationLink);
            context.setVariable("expiryHours", expiryHours);
            
            String htmlContent = templateEngine.process("verification-email", context);
            
            sendEmail(user.getEmail(), "Verify Your Email", htmlContent);
            
            log.info("Verification email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", user.getEmail(), e);
            // Don't throw exception to prevent transaction rollback
        }
    }
    
    @Override
    @Async
    public void sendPasswordResetEmail(User user, String token) {
        try {
            String resetLink = frontendUrl + "/reset-password?token=" + token;
            long expiryHours = resetTokenExpiry / (1000 * 60 * 60);
            
            Context context = new Context();
            context.setVariable("userName", user.getFullName());
            context.setVariable("resetLink", resetLink);
            context.setVariable("expiryHours", expiryHours);
            
            String htmlContent = templateEngine.process("password-reset-email", context);
            
            sendEmail(user.getEmail(), "Reset Your Password", htmlContent);
            
            log.info("Password reset email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", user.getEmail(), e);
            // Don't throw exception to prevent transaction rollback
        }
    }
    
    @Override
    @Async
    public void sendWelcomeEmail(User user) {
        try {
            Context context = new Context();
            context.setVariable("userName", user.getFullName());
            
            String htmlContent = templateEngine.process("welcome-email", context);
            
            sendEmail(user.getEmail(), "Welcome!", htmlContent);
            
            log.info("Welcome email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", user.getEmail(), e);
            // Don't throw exception to prevent transaction rollback
        }
    }
    
    private void sendEmail(String to, String subject, String htmlContent) throws MessagingException {
        if (fromEmail == null || fromEmail.isBlank()) {
            log.warn("Email not sent - fromEmail is not configured. To: {}, Subject: {}", to, subject);
            return;
        }
        
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        
        mailSender.send(message);
    }
}
