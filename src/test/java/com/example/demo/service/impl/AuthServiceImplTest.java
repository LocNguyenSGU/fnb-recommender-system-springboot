package com.example.demo.service.impl;

import com.example.demo.dto.request.LoginRequest;
import com.example.demo.dto.request.RegisterRequest;
import com.example.demo.dto.response.AuthResponse;
import com.example.demo.dto.response.MessageResponse;
import com.example.demo.exception.DuplicateResourceException;
import com.example.demo.exception.EmailNotVerifiedException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.exception.UnauthorizedException;
import com.example.demo.model.EmailVerificationToken;
import com.example.demo.model.User;
import com.example.demo.repository.EmailVerificationTokenRepository;
import com.example.demo.repository.PasswordResetTokenRepository;
import com.example.demo.repository.RefreshTokenRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtTokenService;
import com.example.demo.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    
    @Mock
    private EmailVerificationTokenRepository emailVerificationTokenRepository;
    
    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private JwtTokenService jwtTokenService;
    
    @Mock
    private EmailService emailService;
    
    @InjectMocks
    private AuthServiceImpl authService;
    
    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "verificationTokenExpiry", 86400000L);
        ReflectionTestUtils.setField(authService, "resetTokenExpiry", 3600000L);
        ReflectionTestUtils.setField(authService, "refreshTokenExpiry", 604800000L);
    }
    
    @Test
    void testRegister_Success() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("Password123");
        request.setFullName("Test User");
        request.setPhone("1234567890");
        
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });
        
        // When
        MessageResponse response = authService.register(request);
        
        // Then
        assertNotNull(response);
        assertTrue(response.getMessage().contains("Verification email sent"));
        
        verify(userRepository).findByEmail(request.getEmail());
        verify(userRepository).save(any(User.class));
        verify(emailVerificationTokenRepository).save(any(EmailVerificationToken.class));
        verify(emailService).sendVerificationEmail(any(User.class), anyString());
    }
    
    @Test
    void testRegister_DuplicateEmail() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@example.com");
        request.setPassword("Password123");
        request.setFullName("Test User");
        
        User existingUser = new User();
        existingUser.setEmail(request.getEmail());
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(existingUser));
        
        // When & Then
        assertThrows(DuplicateResourceException.class, () -> authService.register(request));
        
        verify(userRepository).findByEmail(request.getEmail());
        verify(userRepository, never()).save(any(User.class));
        verify(emailService, never()).sendVerificationEmail(any(User.class), anyString());
    }
    
    @Test
    void testVerifyEmail_Success() {
        // Given
        String token = "valid-token";
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setIsVerified(false);
        
        EmailVerificationToken verificationToken = new EmailVerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setExpiresAt(LocalDateTime.now().plusHours(1));
        
        when(emailVerificationTokenRepository.findByToken(token)).thenReturn(Optional.of(verificationToken));
        when(userRepository.save(any(User.class))).thenReturn(user);
        
        // When
        MessageResponse response = authService.verifyEmail(token);
        
        // Then
        assertNotNull(response);
        assertEquals("Email verified successfully", response.getMessage());
        assertTrue(user.getIsVerified());
        
        verify(emailVerificationTokenRepository).findByToken(token);
        verify(userRepository).save(user);
        verify(emailVerificationTokenRepository).delete(verificationToken);
        verify(emailService).sendWelcomeEmail(user);
    }
    
    @Test
    void testLogin_Success() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("Password123");
        
        User user = new User();
        user.setId(1L);
        user.setEmail(request.getEmail());
        user.setPassword("encoded-password");
        user.setFullName("Test User");
        user.setIsVerified(true);
        user.setRole("user");
        
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(true);
        when(jwtTokenService.generateAccessToken(user)).thenReturn("access-token");
        when(jwtTokenService.generateRefreshToken(user)).thenReturn("refresh-token");
        
        // When
        AuthResponse response = authService.login(request);
        
        // Then
        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertNotNull(response.getUser());
        assertEquals(user.getEmail(), response.getUser().getEmail());
        
        verify(userRepository).findByEmail(request.getEmail());
        verify(passwordEncoder).matches(request.getPassword(), user.getPassword());
        verify(jwtTokenService).generateAccessToken(user);
        verify(jwtTokenService).generateRefreshToken(user);
        verify(refreshTokenRepository).save(any());
    }
    
    @Test
    void testLogin_InvalidCredentials() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("WrongPassword");
        
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword("encoded-password");
        
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(false);
        
        // When & Then
        assertThrows(UnauthorizedException.class, () -> authService.login(request));
        
        verify(userRepository).findByEmail(request.getEmail());
        verify(passwordEncoder).matches(request.getPassword(), user.getPassword());
        verify(jwtTokenService, never()).generateAccessToken(any());
    }
    
    @Test
    void testLogin_EmailNotVerified() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("Password123");
        
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword("encoded-password");
        user.setIsVerified(false);
        
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(true);
        
        // When & Then
        assertThrows(EmailNotVerifiedException.class, () -> authService.login(request));
        
        verify(userRepository).findByEmail(request.getEmail());
        verify(passwordEncoder).matches(request.getPassword(), user.getPassword());
        verify(jwtTokenService, never()).generateAccessToken(any());
    }
    
    @Test
    void testForgotPassword_Success() {
        // Given
        String email = "test@example.com";
        User user = new User();
        user.setId(1L);
        user.setEmail(email);
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        
        // When
        MessageResponse response = authService.forgotPassword(email);
        
        // Then
        assertNotNull(response);
        assertTrue(response.getMessage().contains("password reset link has been sent"));
        
        verify(userRepository).findByEmail(email);
        verify(passwordResetTokenRepository).deleteByUser(user);
        verify(passwordResetTokenRepository).save(any());
        verify(emailService).sendPasswordResetEmail(eq(user), anyString());
    }
    
    @Test
    void testForgotPassword_UserNotFound() {
        // Given
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        
        // When
        MessageResponse response = authService.forgotPassword(email);
        
        // Then
        assertNotNull(response);
        assertTrue(response.getMessage().contains("password reset link has been sent"));
        
        verify(userRepository).findByEmail(email);
        verify(emailService, never()).sendPasswordResetEmail(any(), anyString());
    }
    
    @Test
    void testResendVerificationEmail_Success() {
        // Given
        String email = "test@example.com";
        User user = new User();
        user.setId(1L);
        user.setEmail(email);
        user.setIsVerified(false);
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        
        // When
        MessageResponse response = authService.resendVerificationEmail(email);
        
        // Then
        assertNotNull(response);
        assertEquals("Verification email resent", response.getMessage());
        
        verify(userRepository).findByEmail(email);
        verify(emailVerificationTokenRepository).save(any());
        verify(emailService).sendVerificationEmail(eq(user), anyString());
    }
    
    @Test
    void testResendVerificationEmail_UserNotFound() {
        // Given
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> authService.resendVerificationEmail(email));
        
        verify(userRepository).findByEmail(email);
        verify(emailService, never()).sendVerificationEmail(any(), anyString());
    }
}
