# Authentication System Design

**Date:** February 22, 2026  
**Author:** Design Session  
**Status:** Approved

## Overview

Comprehensive authentication system for the Spring Boot application including:
- JWT-based authentication (stateless)
- Email verification for new users
- Password reset functionality
- OAuth2 integration (Google, Facebook)
- Refresh token rotation for security

## Architecture Approach

**Selected: Layered Architecture with Spring Security + JWT**

This approach provides:
- Clear separation between security layer and business logic
- Reusable JWT infrastructure for multiple clients (web, mobile, API)
- Standard Spring Security filters and OAuth2 client
- Easy to test and maintain

## Database Schema Changes

### New Tables

#### 1. email_verification_tokens
```sql
CREATE TABLE email_verification_tokens (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  token VARCHAR(255) UNIQUE NOT NULL,
  expires_at TIMESTAMP NOT NULL,
  created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_email_verification_user ON email_verification_tokens(user_id);
CREATE INDEX idx_email_verification_token ON email_verification_tokens(token);
```

**Purpose:** Store temporary tokens for email verification (24-hour expiry)

#### 2. refresh_tokens
```sql
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
```

**Purpose:** Store refresh tokens with expiry tracking (7-day expiry), allows token revocation

### Table Updates

#### users table
Add field:
```sql
ALTER TABLE users ADD COLUMN is_verified BOOLEAN DEFAULT FALSE;
```

**Purpose:** Track email verification status

**Note:** `password_reset_tokens` table already exists in schema

## Core Components

### 1. JWT Token Service

**Responsibility:** Manage JWT token lifecycle

- **Access Token:**
  - Expiry: 15 minutes
  - Claims: userId, email, role
  - Algorithm: HMAC SHA-256
  
- **Refresh Token:**
  - Expiry: 7 days
  - Stored in database
  - Allows revocation on logout/security breach
  
- **Library:** `io.jsonwebtoken:jjwt-api`, `jjwt-impl`, `jjwt-jackson`

**Key Methods:**
- `generateAccessToken(User user)`
- `generateRefreshToken(User user)`
- `validateToken(String token)`
- `getUserIdFromToken(String token)`

### 2. Spring Security Configuration

**Security Filter Chain:**

- **JWT Authentication Filter:** 
  - Intercepts all requests
  - Extracts JWT from `Authorization: Bearer <token>` header
  - Validates token and sets authentication context
  
- **Public Endpoints:**
  - `/api/auth/**` - No authentication required
  
- **Protected Endpoints:**
  - All other endpoints require valid JWT
  
- **Role-Based Access:**
  - Use `@PreAuthorize("hasRole('ADMIN')")` for admin endpoints
  
- **CORS Configuration:**
  - Allow credentials and specific origins
  - Configure allowed methods and headers

### 3. Custom UserDetailsService

**Implementation:** `CustomUserDetailsService implements UserDetailsService`

**Responsibility:**
- Load user from database by email/username
- Map User entity to Spring Security's UserDetails
- Include roles and authorities
- Verify `is_verified` status

### 4. OAuth2 Integration

**Using:** Spring Security OAuth2 Client

**Providers:**
- **Google OAuth2:** Client ID/Secret from Google Console
- **Facebook OAuth2:** App ID/Secret from Facebook Developers

**OAuth2 Success Handler:**
- After OAuth success, create or find user by email
- Link OAuth provider to user account
- Set `is_verified=true` (OAuth users are auto-verified)
- Generate JWT tokens
- Redirect to frontend with tokens

**User Linking Strategy:**
- If email already exists: Link OAuth provider to existing user
- If new email: Create new user with OAuth provider

## API Endpoints

### Authentication Controller (`/api/auth/**`)

#### POST /api/auth/register
Register new user account

**Request:**
```json
{
  "email": "user@example.com",
  "password": "SecurePass123!",
  "fullName": "John Doe",
  "phone": "0123456789"
}
```

**Response:** `200 OK`
```json
{
  "message": "Verification email sent to user@example.com"
}
```

**Errors:**
- `409 CONFLICT` - Email already exists (DuplicateResourceException)
- `400 BAD_REQUEST` - Invalid input (BadRequestException)

---

#### POST /api/auth/verify-email
Verify email address with token

**Request:**
```json
{
  "token": "uuid-token-here"
}
```

**Response:** `200 OK`
```json
{
  "message": "Email verified successfully"
}
```

**Errors:**
- `400 BAD_REQUEST` - Invalid or expired token
- `404 NOT_FOUND` - Token not found

---

#### POST /api/auth/resend-verification
Resend verification email

**Request:**
```json
{
  "email": "user@example.com"
}
```

**Response:** `200 OK`
```json
{
  "message": "Verification email resent"
}
```

**Errors:**
- `404 NOT_FOUND` - User not found
- `400 BAD_REQUEST` - Email already verified

---

#### POST /api/auth/login
Login with credentials

**Request:**
```json
{
  "email": "user@example.com",
  "password": "SecurePass123!"
}
```

**Response:** `200 OK`
```json
{
  "accessToken": "eyJhbGc...",
  "refreshToken": "eyJhbGc...",
  "user": {
    "id": 1,
    "email": "user@example.com",
    "fullName": "John Doe",
    "role": "user"
  }
}
```

**Errors:**
- `401 UNAUTHORIZED` - Invalid credentials
- `401 UNAUTHORIZED` - Email not verified (EmailNotVerifiedException)
- `404 NOT_FOUND` - User not found

---

#### POST /api/auth/refresh
Refresh access token

**Request:**
```json
{
  "refreshToken": "eyJhbGc..."
}
```

**Response:** `200 OK`
```json
{
  "accessToken": "eyJhbGc...",
  "refreshToken": "eyJhbGc..."
}
```

**Errors:**
- `401 UNAUTHORIZED` - Invalid or expired refresh token
- `404 NOT_FOUND` - Token not found in database

---

#### POST /api/auth/logout
Logout and invalidate tokens

**Headers:**
```
Authorization: Bearer <access-token>
```

**Request:**
```json
{
  "refreshToken": "eyJhbGc..."
}
```

**Response:** `200 OK`
```json
{
  "message": "Logged out successfully"
}
```

---

#### POST /api/auth/forgot-password
Request password reset

**Request:**
```json
{
  "email": "user@example.com"
}
```

**Response:** `200 OK`
```json
{
  "message": "Password reset email sent"
}
```

**Note:** Always returns success even if email not found (security best practice)

---

#### POST /api/auth/reset-password
Reset password with token

**Request:**
```json
{
  "token": "reset-token-here",
  "newPassword": "NewSecurePass123!"
}
```

**Response:** `200 OK`
```json
{
  "message": "Password reset successfully"
}
```

**Errors:**
- `400 BAD_REQUEST` - Invalid or expired token
- `400 BAD_REQUEST` - Invalid password format

---

#### GET /api/auth/oauth2/google
Initiate Google OAuth2 flow

**Response:** Redirect to Google OAuth consent screen

---

#### GET /api/auth/oauth2/facebook
Initiate Facebook OAuth2 flow

**Response:** Redirect to Facebook OAuth consent screen

---

#### GET /api/auth/oauth2/callback
OAuth2 callback handler

**Query Parameters:**
- `code` - Authorization code from OAuth provider
- `state` - CSRF protection token
- `provider` - google or facebook

**Response:** Redirect to frontend with tokens
```
{frontendUrl}/auth/callback?accessToken=...&refreshToken=...
```

## Error Handling

**Using Existing Exception Framework:**

- `UnauthorizedException` - Invalid credentials, expired tokens
- `DuplicateResourceException` - Email already exists
- `BadRequestException` - Invalid input
- `ResourceNotFoundException` - User not found

**New Exceptions:**

- `EmailNotVerifiedException extends UnauthorizedException` - Email not verified
- `TokenExpiredException extends UnauthorizedException` - Token expired

**Error Response Format** (existing):
```json
{
  "timestamp": "2026-02-22T10:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Email chưa được xác thực",
  "path": "/api/auth/login"
}
```

## Email Service

### Configuration

- **Provider:** Spring Mail with Gmail SMTP
- **Template Engine:** Thymeleaf
- **Delivery:** Async with `@Async` annotation
- **Retry Logic:** Max 3 attempts on failure

### Email Templates

**Location:** `src/main/resources/templates/email/`

#### 1. verification-email.html
- Purpose: Email verification
- Variables: `userName`, `verificationLink`, `expiryHours` (24)
- Link format: `{frontendUrl}/verify-email?token={token}`

#### 2. password-reset-email.html
- Purpose: Password reset
- Variables: `userName`, `resetLink`, `expiryHours` (1)
- Link format: `{frontendUrl}/reset-password?token={token}`

#### 3. welcome-email.html
- Purpose: Welcome message after verification
- Variables: `userName`

### SMTP Configuration

**application.properties:**
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

app.frontend.url=${FRONTEND_URL:http://localhost:3000}
```

## Security Flows

### 1. Registration Flow

1. User submits registration form
2. Validate input (email format, password strength)
3. Check email uniqueness
4. Hash password with BCrypt (strength 12)
5. Create user with `is_verified=false`
6. Generate verification token (UUID, 24h expiry)
7. Save token to `email_verification_tokens`
8. Send verification email asynchronously
9. Return success message

### 2. Email Verification Flow

1. User clicks verification link in email
2. Frontend extracts token, calls `/api/auth/verify-email`
3. Backend validates token exists and not expired
4. Set user `is_verified=true`
5. Delete verification token
6. Send welcome email
7. Return success message

### 3. Login Flow

1. User submits credentials
2. Find user by email
3. Check `is_verified=true`, throw `EmailNotVerifiedException` if false
4. Verify password with BCrypt
5. Generate access token (15 min expiry)
6. Generate refresh token (7 day expiry)
7. Save refresh token to database
8. Return tokens + user info

### 4. Token Refresh Flow

1. Frontend detects access token expiring soon
2. Call `/api/auth/refresh` with refresh token
3. Validate refresh token in database
4. Check token not expired
5. Generate new access token
6. Generate new refresh token (rotation)
7. Delete old refresh token, save new one
8. Return new tokens

### 5. OAuth2 Flow

1. User clicks "Login with Google/Facebook"
2. Redirect to OAuth provider consent screen
3. User approves, provider redirects back with code
4. Exchange code for user profile
5. Find user by email or create new user
6. Link OAuth provider (google_id/facebook_id)
7. Set `is_verified=true` (OAuth users auto-verified)
8. Generate JWT tokens
9. Redirect to frontend with tokens in URL

### 6. Password Reset Flow

1. User requests password reset
2. Find user by email
3. Generate reset token (UUID, 1h expiry)
4. Save to `password_reset_tokens`
5. Send reset email
6. Return success (always, even if email not found)
7. User clicks link, submits new password
8. Validate token and expiry
9. Hash new password, update user
10. Delete reset token
11. Invalidate all refresh tokens for security

## Security Best Practices

### Password Security
- **Hashing:** BCrypt with strength 12
- **Validation:** Min 8 chars, mix of letters, numbers, symbols
- **Storage:** Never store plain text

### Token Security
- **Generation:** Cryptographically secure random (SecureRandom)
- **Access Token:** Short-lived (15 min) to limit exposure
- **Refresh Token:** Rotation on use (delete old, create new)
- **Storage:** Database storage allows revocation

### Rate Limiting
- **Login Attempts:** Max 5 per IP per 15 minutes
- **Email Sending:** Max 3 verification emails per hour per user
- **Implementation:** Spring AOP + Redis/in-memory cache

### Additional Security
- **HTTPS Only:** Enforce in production
- **CORS:** Restrict to specific origins
- **CSRF:** Token validation for state-changing operations
- **Token Cleanup:** Daily cron job to delete expired tokens
- **Audit Log:** Log all authentication events

## Testing Strategy

### Unit Tests

**JWT Token Service:**
- Token generation and validation
- Expiry handling
- Claims extraction
- Invalid token scenarios

**Email Service:**
- Template rendering
- Email sending (mocked)
- Retry logic
- Error handling

**Password Encoding:**
- BCrypt hashing
- Password verification

### Integration Tests

**Registration Flow:**
- Successful registration
- Duplicate email handling
- Email verification token creation
- Invalid input validation

**Login Flow:**
- Successful login with verified email
- Failed login with unverified email
- Invalid credentials
- Token generation

**Token Refresh Flow:**
- Valid refresh token
- Expired refresh token
- Invalid refresh token
- Token rotation

**Password Reset Flow:**
- Request reset email
- Reset with valid token
- Reset with expired token
- Invalid token handling

**OAuth2 Flow:**
- Google OAuth callback
- Facebook OAuth callback
- New user creation
- Existing user linking

### Security Tests

- Unauthorized access to protected endpoints
- Expired access token
- Tampered JWT signature
- SQL injection in login
- XSS in input fields
- Rate limiting effectiveness

### Test Configuration

**Mock Email Sending:**
```java
@MockBean
private JavaMailSender mailSender;
```

**Test Database:**
- Use H2 in-memory database
- Test-specific application.properties
- Setup/teardown for clean state

## Dependencies

Add to `pom.xml`:

```xml
<!-- JWT -->
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

<!-- OAuth2 Client -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>

<!-- Email -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>

<!-- Thymeleaf (if not already present) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
```

## Configuration Properties

**application.properties:**

```properties
# JWT Configuration
jwt.secret=${JWT_SECRET:your-256-bit-secret-key-change-in-production}
jwt.access-token-expiry=900000
# 15 minutes in milliseconds
jwt.refresh-token-expiry=604800000
# 7 days in milliseconds

# Email Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.connectiontimeout=5000
spring.mail.properties.mail.smtp.timeout=5000
spring.mail.properties.mail.smtp.writetimeout=5000

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
# 24 hours
app.token.reset-expiry=3600000
# 1 hour
```

## Implementation Order

1. **Phase 1: Foundation**
   - Database migrations
   - New entity models (RefreshToken, EmailVerificationToken)
   - Repository interfaces
   - Custom exceptions

2. **Phase 2: Core Services**
   - JwtTokenService
   - EmailService with templates
   - AuthService (business logic)

3. **Phase 3: Security Layer**
   - JwtAuthenticationFilter
   - CustomUserDetailsService
   - SecurityConfig updates

4. **Phase 4: API Layer**
   - AuthController endpoints
   - Request/Response DTOs
   - Validation

5. **Phase 5: OAuth2**
   - OAuth2 configuration
   - OAuth2SuccessHandler
   - Provider-specific handling

6. **Phase 6: Additional Features**
   - Rate limiting
   - Token cleanup job
   - Audit logging

7. **Phase 7: Testing**
   - Unit tests for services
   - Integration tests for flows
   - Security tests

## Success Criteria

- [ ] Users can register with email verification
- [ ] Users can verify email via link
- [ ] Users can login with credentials
- [ ] JWT tokens are generated and validated
- [ ] Refresh tokens work correctly
- [ ] Users can reset forgotten password
- [ ] Google OAuth2 login works
- [ ] Facebook OAuth2 login works
- [ ] Protected endpoints require authentication
- [ ] Role-based access control works
- [ ] All tests pass with >80% coverage
- [ ] Rate limiting prevents abuse
- [ ] Email sending is reliable
- [ ] Security best practices implemented

## Future Enhancements

- Two-factor authentication (2FA)
- Remember device functionality
- Social account linking for existing users
- Password strength meter
- Account lockout after failed attempts
- Session management UI
- API key generation for third-party integrations
