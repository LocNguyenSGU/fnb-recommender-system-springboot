package com.example.demo.service.impl;

import com.example.demo.dto.request.*;
import com.example.demo.dto.response.AuthResponse;
import com.example.demo.dto.response.MessageResponse;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.exception.*;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import com.example.demo.security.JwtTokenService;
import com.example.demo.service.AuthService;
import com.example.demo.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final EmailService emailService;
    
    @Value("${app.token.verification-expiry}")
    private Long verificationTokenExpiry;
    
    @Value("${app.token.reset-expiry}")
    private Long resetTokenExpiry;
    
    @Value("${jwt.refresh-token-expiry}")
    private Long refreshTokenExpiry;
    
    @Override
    @Transactional
    public MessageResponse register(RegisterRequest request) {
        // Check if email already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Email already exists");
        }
        
        // Create new user
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setRole("user");
        user.setProvider("local");
        user.setIsVerified(false);
        
        user = userRepository.save(user);
        
        // Generate verification token
        String token = UUID.randomUUID().toString();
        EmailVerificationToken verificationToken = new EmailVerificationToken();
        verificationToken.setUser(user);
        verificationToken.setToken(token);
        verificationToken.setExpiresAt(LocalDateTime.now().plusSeconds(verificationTokenExpiry / 1000));
        emailVerificationTokenRepository.save(verificationToken);
        
        // Send verification email
        emailService.sendVerificationEmail(user, token);
        
        return new MessageResponse("Verification email sent to " + request.getEmail());
    }
    
    @Override
    @Transactional
    public MessageResponse verifyEmail(String token) {
        EmailVerificationToken verificationToken = emailVerificationTokenRepository
                .findByToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid verification token"));
        
        if (verificationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new TokenExpiredException("Verification token has expired");
        }
        
        User user = verificationToken.getUser();
        user.setIsVerified(true);
        userRepository.save(user);
        
        // Delete verification token
        emailVerificationTokenRepository.delete(verificationToken);
        
        // Send welcome email
        emailService.sendWelcomeEmail(user);
        
        return new MessageResponse("Email verified successfully");
    }
    
    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid credentials");
        }
        
//        if (!user.getIsVerified()) {
//            throw new EmailNotVerifiedException("Email not verified. Please check your email.");
//        }
        
        // Generate tokens
        String accessToken = jwtTokenService.generateAccessToken(user);
        String refreshToken = jwtTokenService.generateRefreshToken(user);
        
        // Save refresh token
        RefreshToken refreshTokenEntity = new RefreshToken();
        refreshTokenEntity.setUser(user);
        refreshTokenEntity.setToken(refreshToken);
        refreshTokenEntity.setExpiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpiry / 1000));
        refreshTokenRepository.save(refreshTokenEntity);
        
        // Build response
        UserResponse userResponse = mapToUserResponse(user);
        return new AuthResponse(accessToken, refreshToken, userResponse);
    }
    
    @Override
    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        RefreshToken refreshTokenEntity = refreshTokenRepository
                .findByToken(refreshToken)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));
        
        if (refreshTokenEntity.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshTokenEntity);
            throw new TokenExpiredException("Refresh token has expired");
        }
        
        User user = refreshTokenEntity.getUser();
        
        // Generate new tokens
        String newAccessToken = jwtTokenService.generateAccessToken(user);
        String newRefreshToken = jwtTokenService.generateRefreshToken(user);
        
        // Delete old refresh token
        refreshTokenRepository.delete(refreshTokenEntity);
        
        // Save new refresh token
        RefreshToken newRefreshTokenEntity = new RefreshToken();
        newRefreshTokenEntity.setUser(user);
        newRefreshTokenEntity.setToken(newRefreshToken);
        newRefreshTokenEntity.setExpiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpiry / 1000));
        newRefreshTokenEntity.setLastUsedAt(LocalDateTime.now());
        refreshTokenRepository.save(newRefreshTokenEntity);
        
        UserResponse userResponse = mapToUserResponse(user);
        return new AuthResponse(newAccessToken, newRefreshToken, userResponse);
    }
    
    @Override
    @Transactional
    public MessageResponse logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(refreshTokenRepository::delete);
        
        return new MessageResponse("Logged out successfully");
    }
    
    @Override
    @Transactional
    public MessageResponse forgotPassword(String email) {
        // Always return success for security (don't reveal if email exists)
        userRepository.findByEmail(email).ifPresent(user -> {
            // Delete any existing reset tokens for this user
            passwordResetTokenRepository.deleteByUser(user);
            
            // Generate reset token
            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setUser(user);
            resetToken.setToken(token);
            resetToken.setExpiresAt(LocalDateTime.now().plusSeconds(resetTokenExpiry / 1000));
            passwordResetTokenRepository.save(resetToken);
            
            // Send reset email
            emailService.sendPasswordResetEmail(user, token);
        });
        
        return new MessageResponse("If the email exists, a password reset link has been sent");
    }
    
    @Override
    @Transactional
    public MessageResponse resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid reset token"));
        
        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new TokenExpiredException("Reset token has expired");
        }
        
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        // Delete reset token
        passwordResetTokenRepository.delete(resetToken);
        
        // Invalidate all refresh tokens for security
        refreshTokenRepository.deleteAllByUserId(user.getId());
        
        return new MessageResponse("Password reset successfully");
    }
    
    @Override
    @Transactional
    public MessageResponse resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        if (user.getIsVerified()) {
            throw new BadRequestException("Email already verified");
        }
        
        // Delete any existing verification tokens
        emailVerificationTokenRepository.findByUser(user)
                .ifPresent(emailVerificationTokenRepository::delete);
        
        // Generate new verification token
        String token = UUID.randomUUID().toString();
        EmailVerificationToken verificationToken = new EmailVerificationToken();
        verificationToken.setUser(user);
        verificationToken.setToken(token);
        verificationToken.setExpiresAt(LocalDateTime.now().plusSeconds(verificationTokenExpiry / 1000));
        emailVerificationTokenRepository.save(verificationToken);
        
        // Send verification email
        emailService.sendVerificationEmail(user, token);
        
        return new MessageResponse("Verification email resent");
    }
    
    private UserResponse mapToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setRole(user.getRole());
        response.setPhone(user.getPhone());
        response.setAvatarUrl(user.getAvatarUrl());
        response.setIsVerified(user.getIsVerified());
        return response;
    }
}
