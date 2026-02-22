# Authentication System - Implementation Plan

**Date:** February 22, 2026  
**Design Document:** [2026-02-22-authentication-design.md](./2026-02-22-authentication-design.md)  
**Estimated Duration:** 3-4 days

## Overview

This plan breaks down the authentication system implementation into 7 phases with specific, actionable tasks. Each phase builds on the previous one and can be tested independently.

---

## Phase 1: Foundation & Database Setup

**Duration:** ~3 hours  
**Goal:** Set up database tables and core entity models

### Tasks

#### 1.1 Database Migrations
- [ ] Create migration script for `email_verification_tokens` table
- [ ] Create migration script for `refresh_tokens` table  
- [ ] Add `is_verified` column to `users` table
- [ ] Test migrations on local database
- [ ] Verify indexes are created correctly

**Files to create:**
- `src/main/resources/db/migration/V3__create_email_verification_tokens.sql`
- `src/main/resources/db/migration/V4__create_refresh_tokens.sql`
- `src/main/resources/db/migration/V5__add_is_verified_to_users.sql`

**SQL Scripts:**
```sql
-- V3__create_email_verification_tokens.sql
CREATE TABLE email_verification_tokens (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  token VARCHAR(255) UNIQUE NOT NULL,
  expires_at TIMESTAMP NOT NULL,
  created_at TIMESTAMP DEFAULT NOW()
);
CREATE INDEX idx_email_verification_user ON email_verification_tokens(user_id);
CREATE INDEX idx_email_verification_token ON email_verification_tokens(token);

-- V4__create_refresh_tokens.sql
CREATE TABLE refresh_tokens (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  token VARCHAR(500) UNIQUE NOT NULL,
  expires_at TIMESTAMP NOT NULL,
  created_at TIMESTAMP DEFAULT NOW(),
  last_used_at TIMESTAMP
);
CREATE INDEX idx_refresh_token_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_token_token ON refresh_tokens(token);
CREATE INDEX idx_refresh_token_expires ON refresh_tokens(expires_at);

-- V5__add_is_verified_to_users.sql
ALTER TABLE users ADD COLUMN is_verified BOOLEAN DEFAULT FALSE;
UPDATE users SET is_verified = TRUE WHERE email_verified_at IS NOT NULL;
```

#### 1.2 Entity Models
- [ ] Create `RefreshToken` entity
- [ ] Create `EmailVerificationToken` entity
- [ ] Update `User` entity with `isVerified` field
- [ ] Add proper relationships and cascade options

**Files to create/modify:**
- `src/main/java/com/example/demo/model/RefreshToken.java`
- `src/main/java/com/example/demo/model/EmailVerificationToken.java`
- Modify: `src/main/java/com/example/demo/model/User.java`

#### 1.3 Repository Interfaces
- [ ] Create `RefreshTokenRepository` interface
- [ ] Create `EmailVerificationTokenRepository` interface
- [ ] Add custom query methods (findByToken, deleteExpiredTokens, etc.)

**Files to create:**
- `src/main/java/com/example/demo/repository/RefreshTokenRepository.java`
- `src/main/java/com/example/demo/repository/EmailVerificationTokenRepository.java`

#### 1.4 Custom Exceptions
- [ ] Create `EmailNotVerifiedException` extends `UnauthorizedException`
- [ ] Create `TokenExpiredException` extends `UnauthorizedException`
- [ ] Add exception handlers to `GlobalExceptionHandler`

**Files to create/modify:**
- `src/main/java/com/example/demo/exception/EmailNotVerifiedException.java`
- `src/main/java/com/example/demo/exception/TokenExpiredException.java`
- Modify: `src/main/java/com/example/demo/exception/GlobalExceptionHandler.java`

**Verification Steps:**
- Run migrations and check database schema
- Entity classes compile without errors
- Repositories can be autowired in tests
- Test custom exceptions are caught properly

---

## Phase 2: JWT & Core Services

**Duration:** ~4 hours  
**Goal:** Implement JWT token generation/validation and authentication service

### Tasks

#### 2.1 Add Dependencies
- [ ] Add JJWT dependencies to `pom.xml` (api, impl, jackson)
- [ ] Add spring-boot-starter-oauth2-client
- [ ] Add spring-boot-starter-mail
- [ ] Add thymeleaf (if not present)
- [ ] Run `mvn clean install` to verify

**Files to modify:**
- `pom.xml`

**Dependencies to add:**
```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.3</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```

#### 2.2 JWT Token Service
- [ ] Create `JwtTokenService` class
- [ ] Implement `generateAccessToken(User user)` method
- [ ] Implement `generateRefreshToken(User user)` method
- [ ] Implement `validateToken(String token)` method
- [ ] Implement `getUserIdFromToken(String token)` method
- [ ] Implement `getEmailFromToken(String token)` method
- [ ] Add configuration properties for secret and expiry times

**Files to create:**
- `src/main/java/com/example/demo/security/JwtTokenService.java`

**Key Methods:**
```java
public String generateAccessToken(User user)
public String generateRefreshToken(User user)
public boolean validateToken(String token)
public Long getUserIdFromToken(String token)
public String getEmailFromToken(String token)
private Claims extractAllClaims(String token)
private boolean isTokenExpired(String token)
```

#### 2.3 Configuration Properties
- [ ] Add JWT configuration properties
- [ ] Add email configuration properties
- [ ] Add OAuth2 configuration properties
- [ ] Add application-specific properties (frontend URL, token expiry, etc.)

**Files to modify:**
- `src/main/resources/application.properties`

**Properties to add:**
```properties
# JWT Configuration
jwt.secret=${JWT_SECRET:your-256-bit-secret-key-change-in-production}
jwt.access-token-expiry=900000
jwt.refresh-token-expiry=604800000

# Email Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# OAuth2 Configuration
spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID}
spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET}
spring.security.oauth2.client.registration.google.scope=profile,email

spring.security.oauth2.client.registration.facebook.client-id=${FACEBOOK_APP_ID}
spring.security.oauth2.client.registration.facebook.client-secret=${FACEBOOK_APP_SECRET}
spring.security.oauth2.client.registration.facebook.scope=email,public_profile

# Application Configuration
app.frontend.url=${FRONTEND_URL:http://localhost:3000}
app.token.verification-expiry=86400000
app.token.reset-expiry=3600000
```

#### 2.4 Authentication Service
- [ ] Create `AuthService` interface
- [ ] Create `AuthServiceImpl` implementation
- [ ] Implement `register(RegisterRequest)` method
- [ ] Implement `verifyEmail(String token)` method
- [ ] Implement `login(LoginRequest)` method
- [ ] Implement `refreshToken(String refreshToken)` method
- [ ] Implement `logout(String refreshToken)` method
- [ ] Implement `forgotPassword(String email)` method
- [ ] Implement `resetPassword(String token, String newPassword)` method
- [ ] Implement `resendVerificationEmail(String email)` method

**Files to create:**
- `src/main/java/com/example/demo/service/AuthService.java`
- `src/main/java/com/example/demo/service/impl/AuthServiceImpl.java`

**Dependencies:**
- UserRepository
- RefreshTokenRepository
- EmailVerificationTokenRepository
- PasswordResetTokenRepository (already exists)
- PasswordEncoder
- JwtTokenService
- EmailService (will create in Phase 2.5)

#### 2.5 Email Service
- [ ] Create `EmailService` interface
- [ ] Create `EmailServiceImpl` implementation
- [ ] Implement `sendVerificationEmail(User, String token)` method
- [ ] Implement `sendPasswordResetEmail(User, String token)` method
- [ ] Implement `sendWelcomeEmail(User)` method
- [ ] Configure async execution with `@Async`
- [ ] Add retry logic for failed email sending

**Files to create:**
- `src/main/java/com/example/demo/service/EmailService.java`
- `src/main/java/com/example/demo/service/impl/EmailServiceImpl.java`
- `src/main/java/com/example/demo/config/AsyncConfig.java` (for @EnableAsync)

**Verification Steps:**
- Unit test JWT token generation and validation
- Unit test AuthService methods with mocked dependencies
- Unit test EmailService with mocked JavaMailSender
- Verify tokens expire correctly
- Verify password hashing works

---

## Phase 3: Email Templates

**Duration:** ~2 hours  
**Goal:** Create HTML email templates with Thymeleaf

### Tasks

#### 3.1 Email Template Structure
- [ ] Create templates directory structure
- [ ] Create base email template layout
- [ ] Add CSS styling for emails

**Directories to create:**
- `src/main/resources/templates/email/`

#### 3.2 Verification Email Template
- [ ] Create `verification-email.html` template
- [ ] Add verification link with token
- [ ] Add expiry information (24 hours)
- [ ] Add branding and styling
- [ ] Test template rendering

**File to create:**
- `src/main/resources/templates/email/verification-email.html`

**Template Variables:**
- `userName` - User's full name
- `verificationLink` - Complete URL with token
- `expiryHours` - Token expiry (24)

#### 3.3 Password Reset Email Template
- [ ] Create `password-reset-email.html` template
- [ ] Add reset link with token
- [ ] Add expiry information (1 hour)
- [ ] Add security warning
- [ ] Test template rendering

**File to create:**
- `src/main/resources/templates/email/password-reset-email.html`

**Template Variables:**
- `userName` - User's full name
- `resetLink` - Complete URL with token
- `expiryHours` - Token expiry (1)

#### 3.4 Welcome Email Template
- [ ] Create `welcome-email.html` template
- [ ] Add welcome message
- [ ] Add getting started information
- [ ] Add support contact
- [ ] Test template rendering

**File to create:**
- `src/main/resources/templates/email/welcome-email.html`

**Template Variables:**
- `userName` - User's full name

**Verification Steps:**
- Render each template in unit test
- Verify all variables are replaced correctly
- Test email appearance in email client
- Verify links are clickable

---

## Phase 4: Security Layer

**Duration:** ~4 hours  
**Goal:** Implement Spring Security filters and configuration

### Tasks

#### 4.1 Custom UserDetailsService
- [ ] Create `CustomUserDetailsService` implements `UserDetailsService`
- [ ] Implement `loadUserByUsername(String email)` method
- [ ] Map User entity to UserDetails
- [ ] Include roles and authorities
- [ ] Check `isVerified` status

**File to create:**
- `src/main/java/com/example/demo/security/CustomUserDetailsService.java`

**Dependencies:**
- UserRepository

#### 4.2 JWT Authentication Filter
- [ ] Create `JwtAuthenticationFilter` extends `OncePerRequestFilter`
- [ ] Extract JWT from Authorization header
- [ ] Validate token using JwtTokenService
- [ ] Load user details using CustomUserDetailsService
- [ ] Set authentication in SecurityContext
- [ ] Handle exceptions (expired token, invalid token)

**File to create:**
- `src/main/java/com/example/demo/security/JwtAuthenticationFilter.java`

**Key Logic:**
```java
@Override
protected void doFilterInternal(HttpServletRequest request, 
                                HttpServletResponse response, 
                                FilterChain filterChain) {
    // 1. Extract JWT from Authorization header
    // 2. Validate token
    // 3. Get user ID from token
    // 4. Load user details
    // 5. Create authentication object
    // 6. Set in SecurityContext
    // 7. Continue filter chain
}
```

#### 4.3 JWT Authentication Entry Point
- [ ] Create `JwtAuthenticationEntryPoint` implements `AuthenticationEntryPoint`
- [ ] Handle unauthorized access
- [ ] Return proper error response format

**File to create:**
- `src/main/java/com/example/demo/security/JwtAuthenticationEntryPoint.java`

#### 4.4 Update Security Configuration
- [ ] Update `SecurityConfig` class
- [ ] Add JWT filter to filter chain
- [ ] Configure public endpoints (auth APIs)
- [ ] Configure protected endpoints
- [ ] Add CORS configuration
- [ ] Disable CSRF for JWT (stateless)
- [ ] Configure exception handling

**File to modify:**
- `src/main/java/com/example/demo/config/SecurityConfig.java`

**Security Filter Chain:**
```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/**").permitAll()
            .requestMatchers("/api/admin/**").hasRole("ADMIN")
            .anyRequest().authenticated()
        )
        .sessionManagement(session -> 
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(ex -> 
            ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
        .addFilterBefore(jwtAuthenticationFilter, 
            UsernamePasswordAuthenticationFilter.class);
    
    return http.build();
}
```

#### 4.5 CORS Configuration
- [ ] Create CORS configuration bean
- [ ] Allow specific origins (frontend URL)
- [ ] Allow credentials
- [ ] Configure allowed methods and headers

**Verification Steps:**
- Test public endpoints are accessible without token
- Test protected endpoints require valid token
- Test invalid token returns 401
- Test expired token returns 401
- Test CORS headers are present
- Test authentication is set in SecurityContext

---

## Phase 5: API Layer (DTOs & Controller)

**Duration:** ~3 hours  
**Goal:** Create REST API endpoints with proper validation

### Tasks

#### 5.1 Request DTOs
- [ ] Create `RegisterRequest` DTO
- [ ] Create `LoginRequest` DTO
- [ ] Create `RefreshTokenRequest` DTO
- [ ] Create `LogoutRequest` DTO
- [ ] Create `ForgotPasswordRequest` DTO
- [ ] Create `ResetPasswordRequest` DTO
- [ ] Create `VerifyEmailRequest` DTO
- [ ] Create `ResendVerificationRequest` DTO
- [ ] Add validation annotations (@NotBlank, @Email, @Pattern, etc.)

**Files to create:**
- `src/main/java/com/example/demo/dto/request/RegisterRequest.java`
- `src/main/java/com/example/demo/dto/request/LoginRequest.java`
- `src/main/java/com/example/demo/dto/request/RefreshTokenRequest.java`
- `src/main/java/com/example/demo/dto/request/LogoutRequest.java`
- `src/main/java/com/example/demo/dto/request/ForgotPasswordRequest.java`
- `src/main/java/com/example/demo/dto/request/ResetPasswordRequest.java`
- `src/main/java/com/example/demo/dto/request/VerifyEmailRequest.java`
- `src/main/java/com/example/demo/dto/request/ResendVerificationRequest.java`

**Example Validation:**
```java
@Data
public class RegisterRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", 
             message = "Password must contain uppercase, lowercase, and number")
    private String password;
    
    @NotBlank(message = "Full name is required")
    private String fullName;
    
    private String phone;
}
```

#### 5.2 Response DTOs
- [ ] Create `AuthResponse` DTO (tokens + user info)
- [ ] Create `UserResponse` DTO (user details without sensitive data)
- [ ] Create `MessageResponse` DTO (success messages)

**Files to create:**
- `src/main/java/com/example/demo/dto/response/AuthResponse.java`
- `src/main/java/com/example/demo/dto/response/UserResponse.java`
- `src/main/java/com/example/demo/dto/response/MessageResponse.java`

#### 5.3 Authentication Controller
- [ ] Create `AuthController` class
- [ ] Implement `register` endpoint (POST /api/auth/register)
- [ ] Implement `verifyEmail` endpoint (POST /api/auth/verify-email)
- [ ] Implement `resendVerification` endpoint (POST /api/auth/resend-verification)
- [ ] Implement `login` endpoint (POST /api/auth/login)
- [ ] Implement `refresh` endpoint (POST /api/auth/refresh)
- [ ] Implement `logout` endpoint (POST /api/auth/logout)
- [ ] Implement `forgotPassword` endpoint (POST /api/auth/forgot-password)
- [ ] Implement `resetPassword` endpoint (POST /api/auth/reset-password)
- [ ] Add proper validation with @Valid
- [ ] Add proper error handling

**File to create:**
- `src/main/java/com/example/demo/controller/AuthController.java`

**Controller Structure:**
```java
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest request) {
        // Implementation
    }
    
    @PostMapping("/verify-email")
    public ResponseEntity<MessageResponse> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        // Implementation
    }
    
    // ... other endpoints
}
```

**Verification Steps:**
- Test all endpoints with valid input
- Test validation errors for invalid input
- Test error responses match format
- Test authentication required for protected endpoints
- Use Postman or integration tests to verify

---

## Phase 6: OAuth2 Integration

**Duration:** ~4 hours  
**Goal:** Implement Google and Facebook OAuth2 login

### Tasks

#### 6.1 OAuth2 Configuration
- [ ] Configure Google OAuth2 client in application.properties
- [ ] Configure Facebook OAuth2 client in application.properties
- [ ] Set up redirect URIs in Google Console
- [ ] Set up redirect URIs in Facebook App Settings

**Properties already added in Phase 2.3**

**External Setup Required:**
- Google Console: Create OAuth2 credentials, add authorized redirect URI
- Facebook Developers: Create app, add OAuth redirect URI

#### 6.2 OAuth2 Success Handler
- [ ] Create `OAuth2AuthenticationSuccessHandler` extends `SimpleUrlAuthenticationSuccessHandler`
- [ ] Extract user info from OAuth2 provider
- [ ] Find or create user in database
- [ ] Link OAuth provider (google_id or facebook_id)
- [ ] Set `isVerified=true` (OAuth users auto-verified)
- [ ] Generate JWT tokens
- [ ] Redirect to frontend with tokens in URL

**File to create:**
- `src/main/java/com/example/demo/security/OAuth2AuthenticationSuccessHandler.java`

**Key Logic:**
```java
@Override
public void onAuthenticationSuccess(HttpServletRequest request, 
                                   HttpServletResponse response,
                                   Authentication authentication) {
    // 1. Extract OAuth2User from authentication
    // 2. Get email and provider
    // 3. Find or create user
    // 4. Link OAuth provider
    // 5. Generate tokens
    // 6. Redirect to frontend with tokens
}
```

#### 6.3 OAuth2 Failure Handler
- [ ] Create `OAuth2AuthenticationFailureHandler` extends `SimpleUrlAuthenticationFailureHandler`
- [ ] Handle OAuth2 authentication failures
- [ ] Redirect to frontend with error message

**File to create:**
- `src/main/java/com/example/demo/security/OAuth2AuthenticationFailureHandler.java`

#### 6.4 OAuth2 User Service
- [ ] Create `CustomOAuth2UserService` extends `DefaultOAuth2UserService`
- [ ] Process OAuth2 user after authentication
- [ ] Extract user attributes from different providers
- [ ] Normalize user data from Google/Facebook

**File to create:**
- `src/main/java/com/example/demo/security/CustomOAuth2UserService.java`

#### 6.5 Update Security Configuration
- [ ] Add OAuth2 login configuration
- [ ] Configure success handler
- [ ] Configure failure handler
- [ ] Configure OAuth2 user service

**File to modify:**
- `src/main/java/com/example/demo/config/SecurityConfig.java`

**OAuth2 Configuration:**
```java
http.oauth2Login(oauth2 -> oauth2
    .userInfoEndpoint(userInfo -> 
        userInfo.userService(customOAuth2UserService))
    .successHandler(oAuth2AuthenticationSuccessHandler)
    .failureHandler(oAuth2AuthenticationFailureHandler)
);
```

#### 6.6 OAuth2 Controller Endpoints
- [ ] Add OAuth2 initiate endpoints (if needed for manual control)
- [ ] Document OAuth2 URLs for frontend

**Verification Steps:**
- Test Google OAuth2 login flow end-to-end
- Test Facebook OAuth2 login flow end-to-end
- Verify new user creation with OAuth
- Verify existing user linking with OAuth
- Verify tokens are generated correctly
- Verify redirect to frontend works

---

## Phase 7: Additional Features & Testing

**Duration:** ~4 hours  
**Goal:** Add security features, testing, and polish

### Tasks

#### 7.1 Token Cleanup Job
- [ ] Create scheduled job to delete expired tokens
- [ ] Run daily at 2 AM
- [ ] Clean up expired verification tokens
- [ ] Clean up expired refresh tokens
- [ ] Clean up expired password reset tokens
- [ ] Log cleanup statistics

**File to create:**
- `src/main/java/com/example/demo/scheduled/TokenCleanupJob.java`

**Job Implementation:**
```java
@Component
@EnableScheduling
public class TokenCleanupJob {
    
    @Scheduled(cron = "0 0 2 * * ?") // 2 AM daily
    public void cleanupExpiredTokens() {
        // Delete expired tokens
        // Log results
    }
}
```

#### 7.2 Rate Limiting (Optional but Recommended)
- [ ] Create rate limiting aspect with AOP
- [ ] Implement in-memory rate limiter (or Redis-based)
- [ ] Apply to login endpoint (5 attempts per 15 min per IP)
- [ ] Apply to email sending endpoints
- [ ] Return 429 Too Many Requests when limit exceeded

**Files to create:**
- `src/main/java/com/example/demo/security/RateLimitAspect.java`
- `src/main/java/com/example/demo/security/RateLimiter.java`

#### 7.3 Audit Logging (Optional but Recommended)
- [ ] Create audit log entity
- [ ] Log all authentication events (login, logout, failed attempts)
- [ ] Log token generation/refresh
- [ ] Log password changes
- [ ] Include IP address, user agent, timestamp

**Files to create:**
- `src/main/java/com/example/demo/model/AuditLog.java`
- `src/main/java/com/example/demo/repository/AuditLogRepository.java`
- `src/main/java/com/example/demo/service/AuditService.java`

#### 7.4 Unit Tests
- [ ] Write unit tests for `JwtTokenService`
  - Token generation
  - Token validation
  - Expiry handling
  - Claims extraction
- [ ] Write unit tests for `AuthServiceImpl`
  - Register flow
  - Login flow
  - Token refresh flow
  - Password reset flow
- [ ] Write unit tests for `EmailServiceImpl`
  - Template rendering
  - Email sending (mocked)
- [ ] Aim for >80% code coverage

**Test Files to create:**
- `src/test/java/com/example/demo/security/JwtTokenServiceTest.java`
- `src/test/java/com/example/demo/service/AuthServiceImplTest.java`
- `src/test/java/com/example/demo/service/EmailServiceImplTest.java`

#### 7.5 Integration Tests
- [ ] Write integration tests for registration flow
  - Successful registration
  - Duplicate email handling
  - Email verification
- [ ] Write integration tests for login flow
  - Successful login
  - Failed login (wrong password)
  - Unverified email
- [ ] Write integration tests for token refresh flow
  - Valid refresh token
  - Expired refresh token
  - Invalid refresh token
- [ ] Write integration tests for password reset flow
  - Request reset
  - Reset with valid token
  - Reset with expired token
- [ ] Write integration tests for OAuth2 flow (if possible)

**Test Files to create:**
- `src/test/java/com/example/demo/integration/AuthenticationFlowIntegrationTest.java`
- `src/test/java/com/example/demo/integration/PasswordResetFlowIntegrationTest.java`
- `src/test/java/com/example/demo/integration/TokenRefreshFlowIntegrationTest.java`

#### 7.6 Security Tests
- [ ] Test unauthorized access to protected endpoints
- [ ] Test with expired tokens
- [ ] Test with tampered tokens
- [ ] Test rate limiting effectiveness
- [ ] Test SQL injection attempts in login
- [ ] Test XSS in input fields

**Test File to create:**
- `src/test/java/com/example/demo/security/SecurityTest.java`

#### 7.7 Documentation
- [ ] Update API_DOCUMENTATION.md with authentication endpoints
- [ ] Document request/response formats
- [ ] Document error codes
- [ ] Add Postman collection (optional)
- [ ] Update README with OAuth2 setup instructions

**Files to modify:**
- `API_DOCUMENTATION.md`
- `README.md`

#### 7.8 Environment Configuration
- [ ] Create `.env.example` file with all required environment variables
- [ ] Document how to set up Gmail SMTP
- [ ] Document how to get Google OAuth2 credentials
- [ ] Document how to get Facebook OAuth2 credentials
- [ ] Add security notes about JWT secret

**File to create:**
- `.env.example`

**Example Content:**
```bash
# JWT Configuration
JWT_SECRET=your-256-bit-secret-key-change-in-production
# Generate with: openssl rand -base64 32

# Email Configuration
MAIL_USERNAME=your-gmail@gmail.com
MAIL_PASSWORD=your-app-password
# Get app password: https://support.google.com/accounts/answer/185833

# OAuth2 - Google
GOOGLE_CLIENT_ID=your-google-client-id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=your-google-client-secret
# Get from: https://console.cloud.google.com/apis/credentials

# OAuth2 - Facebook
FACEBOOK_APP_ID=your-facebook-app-id
FACEBOOK_APP_SECRET=your-facebook-app-secret
# Get from: https://developers.facebook.com/apps/

# Application
FRONTEND_URL=http://localhost:3000
```

**Verification Steps:**
- All unit tests pass
- All integration tests pass
- All security tests pass
- Test coverage >80%
- Manual testing of all flows
- Documentation is clear and complete

---

## Testing Checklist

### Manual Testing Flows

#### Registration & Verification
- [ ] Register new user with valid data
- [ ] Register with duplicate email (should fail)
- [ ] Register with invalid email format (should fail)
- [ ] Register with weak password (should fail)
- [ ] Verify email with valid token
- [ ] Verify email with expired token (should fail)
- [ ] Verify email with invalid token (should fail)
- [ ] Resend verification email
- [ ] Receive welcome email after verification

#### Login & Authentication
- [ ] Login with valid credentials
- [ ] Login with unverified email (should fail)
- [ ] Login with wrong password (should fail)
- [ ] Login with non-existent email (should fail)
- [ ] Access protected endpoint with valid token
- [ ] Access protected endpoint without token (should fail 401)
- [ ] Access protected endpoint with expired token (should fail 401)
- [ ] Access protected endpoint with invalid token (should fail 401)

#### Token Refresh
- [ ] Refresh with valid refresh token
- [ ] Refresh with invalid refresh token (should fail)
- [ ] Refresh with expired refresh token (should fail)
- [ ] Old refresh token is invalidated after refresh

#### Logout
- [ ] Logout successfully invalidates refresh token
- [ ] Cannot refresh with logged-out token

#### Password Reset
- [ ] Request password reset for existing email
- [ ] Request password reset for non-existent email (still returns success)
- [ ] Receive password reset email
- [ ] Reset password with valid token
- [ ] Reset password with expired token (should fail)
- [ ] Reset password with invalid token (should fail)
- [ ] All refresh tokens invalidated after password reset
- [ ] Can login with new password

#### OAuth2 Login
- [ ] Login with Google account (new user)
- [ ] Login with Google account (existing user)
- [ ] Login with Facebook account (new user)
- [ ] Login with Facebook account (existing user)
- [ ] OAuth users are auto-verified
- [ ] Tokens are generated after OAuth success
- [ ] Redirect to frontend works

#### Security Features
- [ ] Rate limiting blocks after 5 failed login attempts
- [ ] Rate limiting allows login again after wait period
- [ ] Email rate limiting prevents spam
- [ ] CORS headers allow frontend origin
- [ ] CORS blocks unauthorized origins
- [ ] SQL injection attempts are prevented
- [ ] XSS attempts are sanitized

#### Background Jobs
- [ ] Token cleanup job runs on schedule
- [ ] Expired tokens are deleted
- [ ] Active tokens are not deleted

---

## Deployment Checklist

### Pre-Deployment
- [ ] All tests passing
- [ ] Code reviewed
- [ ] Environment variables documented
- [ ] Database migrations tested
- [ ] JWT secret is secure (not default value)
- [ ] HTTPS configured in production
- [ ] CORS origins set to production URLs
- [ ] Email SMTP credentials configured
- [ ] OAuth2 credentials configured for production
- [ ] Rate limiting enabled
- [ ] Audit logging enabled

### Post-Deployment
- [ ] Smoke test registration flow
- [ ] Smoke test login flow
- [ ] Smoke test password reset flow
- [ ] Smoke test OAuth2 login
- [ ] Monitor error logs
- [ ] Monitor email delivery
- [ ] Monitor token cleanup job
- [ ] Monitor authentication metrics

---

## Rollback Plan

If issues arise after deployment:

1. **Critical Issues (security, data loss):**
   - Immediately rollback to previous version
   - Block affected endpoints via API gateway
   - Investigate and fix in development

2. **Non-Critical Issues (email, OAuth):**
   - Disable affected features via feature flags
   - Fall back to basic authentication only
   - Fix and deploy patch

3. **Database Rollback:**
   - Keep separate migration rollback scripts
   - Test rollback in staging first
   - Backup database before rollback

**Rollback Scripts:**
```sql
-- Rollback V5
ALTER TABLE users DROP COLUMN is_verified;

-- Rollback V4
DROP TABLE refresh_tokens;

-- Rollback V3
DROP TABLE email_verification_tokens;
```

---

## Success Criteria

- [ ] All authentication flows working end-to-end
- [ ] All tests passing (unit + integration + security)
- [ ] Test coverage >80%
- [ ] Documentation complete
- [ ] Security best practices implemented
- [ ] Performance acceptable (login <500ms)
- [ ] Email delivery reliable (>95% success rate)
- [ ] OAuth2 login working for Google and Facebook
- [ ] No security vulnerabilities found in review
- [ ] Code reviewed and approved
- [ ] Successfully deployed to staging
- [ ] Manual testing completed in staging
- [ ] Ready for production deployment

---

## Timeline

| Phase | Duration | Completion Date |
|-------|----------|-----------------|
| Phase 1: Foundation | 3 hours | Day 1 Morning |
| Phase 2: JWT & Services | 4 hours | Day 1 Afternoon |
| Phase 3: Email Templates | 2 hours | Day 2 Morning |
| Phase 4: Security Layer | 4 hours | Day 2 Afternoon |
| Phase 5: API Layer | 3 hours | Day 3 Morning |
| Phase 6: OAuth2 | 4 hours | Day 3 Afternoon |
| Phase 7: Testing & Polish | 4 hours | Day 4 |

**Total Estimated Time:** 24 hours (3-4 working days)

---

## Notes

- Prioritize security over features
- Test each phase independently before moving to next
- Keep commits small and focused
- Document decisions and trade-offs
- Ask for code review after each phase
- Monitor performance and optimize if needed
- Consider feature flags for gradual rollout
- Have database backups before migrations

## Questions to Resolve Before Starting

1. What is the production frontend URL for CORS and OAuth2 redirects?
2. Do you have Gmail SMTP credentials ready?
3. Do you have Google OAuth2 credentials?
4. Do you have Facebook OAuth2 credentials?
5. Should rate limiting be aggressive (5 attempts) or lenient (10 attempts)?
6. Do you want audit logging from the start or add later?
7. What should be the JWT secret generation strategy?
8. Should refresh tokens be stored in HTTP-only cookies instead of response body?

---

**Ready to begin implementation? Start with Phase 1!**
