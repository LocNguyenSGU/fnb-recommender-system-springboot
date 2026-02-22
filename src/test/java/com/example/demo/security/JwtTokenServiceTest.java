package com.example.demo.security;

import com.example.demo.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenServiceTest {
    
    private JwtTokenService jwtTokenService;
    private String testSecret = "test-secret-key-for-jwt-token-generation-must-be-at-least-256-bits";
    private Long accessTokenExpiry = 900000L; // 15 minutes
    private Long refreshTokenExpiry = 604800000L; // 7 days
    
    @BeforeEach
    void setUp() {
        jwtTokenService = new JwtTokenService();
        ReflectionTestUtils.setField(jwtTokenService, "secret", testSecret);
        ReflectionTestUtils.setField(jwtTokenService, "accessTokenExpiry", accessTokenExpiry);
        ReflectionTestUtils.setField(jwtTokenService, "refreshTokenExpiry", refreshTokenExpiry);
    }
    
    @Test
    void testGenerateAccessToken() {
        // Given
        User user = createTestUser();
        
        // When
        String token = jwtTokenService.generateAccessToken(user);
        
        // Then
        assertNotNull(token);
        assertTrue(token.length() > 0);
        
        // Verify token contains expected claims
        Claims claims = parseToken(token);
        assertEquals(user.getEmail(), claims.getSubject());
        assertEquals(user.getId().intValue(), claims.get("userId"));
        assertEquals(user.getEmail(), claims.get("email"));
        assertEquals(user.getRole(), claims.get("role"));
    }
    
    @Test
    void testGenerateRefreshToken() {
        // Given
        User user = createTestUser();
        
        // When
        String token = jwtTokenService.generateRefreshToken(user);
        
        // Then
        assertNotNull(token);
        assertTrue(token.length() > 0);
        
        // Verify token contains expected claims
        Claims claims = parseToken(token);
        assertEquals(user.getEmail(), claims.getSubject());
        assertEquals(user.getId().intValue(), claims.get("userId"));
        assertEquals("refresh", claims.get("type"));
    }
    
    @Test
    void testValidateToken_ValidToken() {
        // Given
        User user = createTestUser();
        String token = jwtTokenService.generateAccessToken(user);
        
        // When
        boolean isValid = jwtTokenService.validateToken(token);
        
        // Then
        assertTrue(isValid);
    }
    
    @Test
    void testValidateToken_InvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";
        
        // When
        boolean isValid = jwtTokenService.validateToken(invalidToken);
        
        // Then
        assertFalse(isValid);
    }
    
    @Test
    void testValidateToken_ExpiredToken() {
        // Given - Create an expired token
        User user = createTestUser();
        SecretKey key = Keys.hmacShaKeyFor(testSecret.getBytes(StandardCharsets.UTF_8));
        
        String expiredToken = Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("type", "access")
                .issuedAt(new Date(System.currentTimeMillis() - 10000))
                .expiration(new Date(System.currentTimeMillis() - 5000)) // Expired 5 seconds ago
                .signWith(key)
                .compact();
        
        // When
        boolean isValid = jwtTokenService.validateToken(expiredToken);
        
        // Then
        assertFalse(isValid);
    }
    
    @Test
    void testGetUserIdFromToken() {
        // Given
        User user = createTestUser();
        String token = jwtTokenService.generateAccessToken(user);
        
        // When
        Long userId = jwtTokenService.getUserIdFromToken(token);
        
        // Then
        assertEquals(user.getId(), userId);
    }
    
    @Test
    void testGetEmailFromToken() {
        // Given
        User user = createTestUser();
        String token = jwtTokenService.generateAccessToken(user);
        
        // When
        String email = jwtTokenService.getEmailFromToken(token);
        
        // Then
        assertEquals(user.getEmail(), email);
    }
    
    @Test
    void testAccessTokenExpiry() {
        // Given
        User user = createTestUser();
        String token = jwtTokenService.generateAccessToken(user);
        
        // When
        Claims claims = parseToken(token);
        Date expiration = claims.getExpiration();
        Date issuedAt = claims.getIssuedAt();
        
        // Then
        long actualExpiry = expiration.getTime() - issuedAt.getTime();
        assertEquals(accessTokenExpiry, actualExpiry);
    }
    
    @Test
    void testRefreshTokenExpiry() {
        // Given
        User user = createTestUser();
        String token = jwtTokenService.generateRefreshToken(user);
        
        // When
        Claims claims = parseToken(token);
        Date expiration = claims.getExpiration();
        Date issuedAt = claims.getIssuedAt();
        
        // Then
        long actualExpiry = expiration.getTime() - issuedAt.getTime();
        assertEquals(refreshTokenExpiry, actualExpiry);
    }
    
    private User createTestUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setFullName("Test User");
        user.setRole("user");
        return user;
    }
    
    private Claims parseToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(testSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
