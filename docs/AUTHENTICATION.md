# Authentication System Documentation

## Overview

This authentication system provides comprehensive user authentication and authorization features including:

- ✅ User Registration with Email Verification
- ✅ Login with JWT (Access + Refresh Tokens)
- ✅ Password Reset via Email
- ✅ Email Resend Functionality
- ✅ OAuth2 Integration (Google & Facebook)
- ✅ Token Refresh Mechanism
- ✅ Secure Password Hashing (BCrypt)
- ✅ Automatic Token Cleanup Job

## Technology Stack

- **Spring Boot** 4.0.3
- **Spring Security** with JWT
- **JWT Library** (JJWT 0.12.3)
- **PostgreSQL** Database
- **Spring Mail** for Email
- **Thymeleaf** for Email Templates
- **OAuth2** Client for Social Login

## API Endpoints

### Public Endpoints

#### 1. Register
```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "Password123",
  "fullName": "John Doe",
  "phone": "1234567890"
}

Response: 201 Created
{
  "message": "Verification email sent to user@example.com"
}
```

**Password Requirements:**
- Minimum 8 characters
- At least one uppercase letter
- At least one lowercase letter
- At least one number

#### 2. Verify Email
```http
GET /api/auth/verify-email?token=<verification-token>

Response: 200 OK
{
  "message": "Email verified successfully"
}
```

#### 3. Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "Password123"
}

Response: 200 OK
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "email": "user@example.com",
    "fullName": "John Doe",
    "role": "user",
    "phone": "1234567890",
    "avatarUrl": null,
    "isVerified": true
  }
}
```

#### 4. Refresh Token
```http
POST /api/auth/refresh-token
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}

Response: 200 OK
{
  "accessToken": "new-access-token",
  "refreshToken": "new-refresh-token",
  "user": { ... }
}
```

#### 5. Logout
```http
POST /api/auth/logout
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}

Response: 200 OK
{
  "message": "Logged out successfully"
}
```

#### 6. Forgot Password
```http
POST /api/auth/forgot-password
Content-Type: application/json

{
  "email": "user@example.com"
}

Response: 200 OK
{
  "message": "If the email exists, a password reset link has been sent"
}
```

#### 7. Reset Password
```http
POST /api/auth/reset-password
Content-Type: application/json

{
  "token": "reset-token-from-email",
  "newPassword": "NewPassword123"
}

Response: 200 OK
{
  "message": "Password reset successfully"
}
```

#### 8. Resend Verification Email
```http
POST /api/auth/resend-verification
Content-Type: application/json

{
  "email": "user@example.com"
}

Response: 200 OK
{
  "message": "Verification email resent"
}
```

### Protected Endpoints

All protected endpoints require the `Authorization` header:

```http
Authorization: Bearer <access-token>
```

## OAuth2 Social Login

### Google Login

**Frontend URL:**
```
http://localhost:8080/oauth2/authorization/google
```

When user clicks this link:
1. User is redirected to Google login
2. After successful authentication, redirected back to app
3. App generates JWT tokens
4. Redirects to frontend with tokens:
   ```
   http://localhost:3000/oauth2/redirect?accessToken=xxx&refreshToken=xxx
   ```

### Facebook Login

**Frontend URL:**
```
http://localhost:8080/oauth2/authorization/facebook
```

Same flow as Google login.

## Setup Instructions

### 1. Database Setup

Create PostgreSQL database:
```sql
CREATE DATABASE fnb_recommender;
```

Run Flyway migrations (automatically on startup):
- V1: Initial schema (users, etc.)
- V2: Additional tables
- V3: Email verification tokens table
- V4: Refresh tokens table
- V5: Add is_verified column to users
- V6: Password reset tokens table

### 2. Environment Variables

Copy `.env.example` to `.env` and configure:

```bash
# JWT Secret (Generate with: openssl rand -base64 32)
JWT_SECRET=your-strong-secret-key-here

# Gmail SMTP
MAIL_USERNAME=your-gmail@gmail.com
MAIL_PASSWORD=your-app-password

# Google OAuth2
GOOGLE_CLIENT_ID=your-client-id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=your-client-secret

# Facebook OAuth2
FACEBOOK_APP_ID=your-app-id
FACEBOOK_APP_SECRET=your-app-secret

# Frontend URL
FRONTEND_URL=http://localhost:3000
```

### 3. Gmail SMTP Setup

1. Enable 2-Factor Authentication on your Google account
2. Go to: https://myaccount.google.com/apppasswords
3. Generate an App Password for "Mail"
4. Use the 16-character password in `MAIL_PASSWORD`

### 4. Google OAuth2 Setup

1. Go to: https://console.cloud.google.com/apis/credentials
2. Create OAuth 2.0 Client ID
3. Add Authorized Redirect URI:
   - Development: `http://localhost:8080/login/oauth2/code/google`
   - Production: `https://yourdomain.com/login/oauth2/code/google`
4. Copy Client ID and Client Secret

### 5. Facebook OAuth2 Setup

1. Go to: https://developers.facebook.com/apps/
2. Create or select your app
3. Add "Facebook Login" product
4. Configure Valid OAuth Redirect URIs:
   - Development: `http://localhost:8080/login/oauth2/code/facebook`
   - Production: `https://yourdomain.com/login/oauth2/code/facebook`
5. Copy App ID and App Secret

### 6. Run the Application

```bash
./mvnw spring-boot:run
```

Application will start on `http://localhost:8080`

## Token Management

### Access Token
- **Expiry:** 15 minutes (900,000 ms)
- **Type:** Short-lived
- **Usage:** Include in Authorization header for API requests
- **Claims:** userId, email, type=access

### Refresh Token
- **Expiry:** 7 days (604,800,000 ms)
- **Type:** Long-lived
- **Usage:** Request new access token when expired
- **Storage:** Database (refresh_tokens table)
- **Security:** Invalidated on logout and password reset

### Token Cleanup

Automated cleanup job runs daily at 2 AM:
- Deletes expired email verification tokens
- Deletes expired password reset tokens
- Deletes expired refresh tokens

Manual cleanup:
```java
@Autowired
private TokenCleanupJob tokenCleanupJob;

tokenCleanupJob.cleanupNow();
```

## Security Features

### 1. Password Security
- BCrypt hashing with strength 10
- Minimum 8 characters
- Must contain uppercase, lowercase, and number

### 2. Token Security
- HMAC SHA-256 signing algorithm
- Secure random secret key (256-bit minimum)
- Short-lived access tokens
- Refresh token rotation on each use

### 3. Email Security
- Verification required before login
- Password reset tokens expire in 1 hour
- Verification tokens expire in 24 hours
- One-time use tokens (deleted after use)

### 4. OAuth2 Security
- Users authenticated via OAuth are auto-verified
- Provider-specific user IDs stored
- Secure redirect handling

### 5. CORS Configuration
- Configured origins: `http://localhost:3000`, `http://localhost:5173`
- Credentials allowed
- Proper headers configuration

## Error Handling

### Common Error Responses

**401 Unauthorized:**
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid credentials",
  "timestamp": "2026-02-22T10:30:00"
}
```

**400 Bad Request:**
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Email already exists",
  "timestamp": "2026-02-22T10:30:00"
}
```

**409 Conflict:**
```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Email already exists",
  "timestamp": "2026-02-22T10:30:00"
}
```

## Testing

### Run Unit Tests
```bash
./mvnw test
```

### Run Integration Tests
```bash
./mvnw verify
```

### Test Coverage
- JWT Token Service: Comprehensive token generation and validation tests
- Auth Service: Registration, login, password reset flows
- Target Coverage: >80%

## Frontend Integration

### 1. Login Flow
```javascript
const response = await fetch('http://localhost:8080/api/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ email, password })
});

const { accessToken, refreshToken, user } = await response.json();

// Store tokens
localStorage.setItem('accessToken', accessToken);
localStorage.setItem('refreshToken', refreshToken);
```

### 2. API Request with Token
```javascript
const response = await fetch('http://localhost:8080/api/protected-resource', {
  headers: {
    'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
  }
});
```

### 3. Token Refresh
```javascript
async function refreshAccessToken() {
  const refreshToken = localStorage.getItem('refreshToken');
  
  const response = await fetch('http://localhost:8080/api/auth/refresh-token', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ refreshToken })
  });
  
  const { accessToken, refreshToken: newRefreshToken } = await response.json();
  
  localStorage.setItem('accessToken', accessToken);
  localStorage.setItem('refreshToken', newRefreshToken);
}
```

### 4. OAuth2 Login Button
```javascript
// Google Login
window.location.href = 'http://localhost:8080/oauth2/authorization/google';

// Facebook Login
window.location.href = 'http://localhost:8080/oauth2/authorization/facebook';
```

### 5. Handle OAuth2 Redirect
```javascript
// On /oauth2/redirect page
const params = new URLSearchParams(window.location.search);
const accessToken = params.get('accessToken');
const refreshToken = params.get('refreshToken');

if (accessToken && refreshToken) {
  localStorage.setItem('accessToken', accessToken);
  localStorage.setItem('refreshToken', refreshToken);
  // Redirect to dashboard
  window.location.href = '/dashboard';
}
```

## Production Deployment Checklist

- [ ] Change `JWT_SECRET` to a strong random value
- [ ] Use HTTPS for all endpoints
- [ ] Update `FRONTEND_URL` to production URL
- [ ] Update CORS allowed origins
- [ ] Configure Gmail SMTP with production credentials
- [ ] Update OAuth2 redirect URIs to production URLs
- [ ] Enable rate limiting on authentication endpoints
- [ ] Set up monitoring for failed login attempts
- [ ] Configure backup strategy for database
- [ ] Set up log aggregation
- [ ] Enable audit logging
- [ ] Configure secrets management (AWS Secrets Manager, etc.)

## Troubleshooting

### Email Not Sending
- Check Gmail SMTP credentials
- Verify App Password is correct (not regular password)
- Check if 2FA is enabled on Google account
- Review email service logs

### OAuth2 Not Working
- Verify redirect URIs match exactly
- Check if OAuth2 app is in production mode (not testing)
- Review OAuth2 scopes configuration
- Inspect browser console for errors

### Token Validation Failing
- Verify JWT secret matches between environments
- Check token expiry times
- Ensure clock synchronization between servers
- Review token format in Authorization header

### Database Connection Issues
- Verify PostgreSQL is running
- Check database credentials
- Ensure database exists
- Review Flyway migration logs

## Support

For issues or questions, please refer to:
- API Documentation: `API_DOCUMENTATION.md`
- Design Document: `docs/plans/2026-02-22-authentication-design.md`
- Implementation Plan: `docs/plans/2026-02-22-authentication-implementation-plan.md`

---

**Last Updated:** February 22, 2026
**Version:** 1.0.0
