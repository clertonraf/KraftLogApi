# Password Recovery Feature

This document describes the password recovery feature implementation in KraftLog API.

## Overview

The password recovery feature allows users to reset their passwords via email. When a user requests password recovery, they receive an email with a unique, time-limited URL that allows them to set a new password.

## Features

- **Secure Token Generation**: Each password reset request generates a unique UUID token
- **24-Hour Expiration**: Reset links expire after 24 hours for security
- **One-Time Use**: Tokens can only be used once to prevent replay attacks
- **Email Notifications**: Users receive password reset links via email
- **Token Cleanup**: Old tokens for the same user are automatically deleted when a new request is made

## API Endpoints

### 1. Request Password Recovery

**Endpoint**: `POST /api/auth/password-recovery`

**Description**: Initiates the password recovery process by sending a reset email to the user.

**Request Body**:
```json
{
  "email": "user@example.com"
}
```

**Response** (200 OK):
```json
{
  "message": "Password recovery email sent successfully"
}
```

**Error Responses**:
- `404 Not Found`: User with the provided email does not exist
- `400 Bad Request`: Invalid email format

### 2. Reset Password

**Endpoint**: `POST /api/auth/password-reset`

**Description**: Resets the user's password using a valid reset token.

**Request Body**:
```json
{
  "token": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "newPassword": "newSecurePassword123"
}
```

**Response** (200 OK):
```json
{
  "message": "Password has been reset successfully"
}
```

**Error Responses**:
- `400 Bad Request`: Invalid token, expired token, or token already used
- `400 Bad Request`: Password too short (minimum 6 characters)

## Configuration

### Environment Variables

The following environment variables must be configured for the password recovery feature to work:

```bash
# SMTP Configuration
SMTP_HOST=smtp.gmail.com          # SMTP server hostname
SMTP_PORT=587                      # SMTP server port (usually 587 for TLS)
SMTP_USERNAME=your-email@gmail.com # SMTP username (email address)
SMTP_PASSWORD=your-app-password    # SMTP password or app-specific password

# Frontend URL
FRONTEND_URL=http://localhost:3000 # Frontend application URL
```

### Gmail Configuration

If using Gmail as your SMTP provider:

1. Enable 2-Factor Authentication on your Google account
2. Generate an App Password:
   - Go to Google Account Settings → Security → 2-Step Verification → App passwords
   - Generate a new app password for "Mail"
   - Use this app password as `SMTP_PASSWORD`

### Other SMTP Providers

For other email providers, adjust the `SMTP_HOST` and `SMTP_PORT` accordingly:

- **SendGrid**: `smtp.sendgrid.net:587`
- **Mailgun**: `smtp.mailgun.org:587`
- **Amazon SES**: `email-smtp.[region].amazonaws.com:587`
- **Office 365**: `smtp.office365.com:587`

## Email Template

The password reset email includes:
- A personalized greeting
- The password reset link with token
- Expiration information (24 hours)
- A note to ignore the email if not requested by the user

Example:
```
Hello,

You have requested to reset your password for your KraftLog account.

Please click on the link below to reset your password:
http://localhost:3000/reset-password?token=a1b2c3d4-e5f6-7890-abcd-ef1234567890

This link will expire in 24 hours.

If you did not request a password reset, please ignore this email.

Best regards,
KraftLog Team
```

## Database Schema

### password_reset_tokens Table

```sql
CREATE TABLE password_reset_tokens (
    id UUID PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id UUID NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_password_reset_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);
```

**Indexes**:
- `idx_password_reset_token`: Fast token lookup
- `idx_password_reset_user_id`: Fast user lookup
- `idx_password_reset_expiry_date`: Efficient cleanup of expired tokens

## Security Considerations

1. **Token Uniqueness**: Each token is a UUID, ensuring uniqueness
2. **Time-Limited**: Tokens expire after 24 hours
3. **One-Time Use**: Tokens are marked as used after successful password reset
4. **Email Validation**: Only registered email addresses can request password recovery
5. **HTTPS**: In production, always use HTTPS to protect tokens in transit
6. **Password Requirements**: Minimum 6 characters (can be adjusted in validation)

## Frontend Integration

The frontend should implement a password reset page that:

1. Extracts the token from the URL query parameter: `/reset-password?token=...`
2. Displays a form for the user to enter a new password
3. Sends a POST request to `/api/auth/password-reset` with the token and new password
4. Handles success and error responses appropriately

Example frontend flow:
```javascript
// Extract token from URL
const urlParams = new URLSearchParams(window.location.search);
const token = urlParams.get('token');

// Submit password reset
async function resetPassword(newPassword) {
  const response = await fetch('/api/auth/password-reset', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ token, newPassword })
  });
  
  if (response.ok) {
    // Redirect to login page
    window.location.href = '/login';
  } else {
    // Show error message
    const error = await response.json();
    alert(error.message);
  }
}
```

## Testing

The implementation includes comprehensive tests:

### Unit Tests
- `PasswordRecoveryServiceTest`: Tests for password recovery service logic
- `EmailServiceTest`: Tests for email sending functionality

### Integration Tests
- `PasswordRecoveryIntegrationTest`: End-to-end tests for the complete flow

Run tests with:
```bash
mvn test
```

## Maintenance

### Token Cleanup

The `PasswordRecoveryService` provides a `cleanupExpiredTokens()` method to remove expired tokens. This can be scheduled to run periodically:

```java
@Scheduled(cron = "0 0 0 * * ?") // Daily at midnight
public void scheduleTokenCleanup() {
    passwordRecoveryService.cleanupExpiredTokens();
}
```

## Troubleshooting

### Emails Not Being Sent

1. **Check SMTP credentials**: Verify `SMTP_USERNAME` and `SMTP_PASSWORD` are correct
2. **Check firewall**: Ensure port 587 is open
3. **Check logs**: Look for error messages in application logs
4. **Test SMTP connection**: Use telnet or a mail client to verify SMTP server is accessible

### Token Not Found

1. **Check expiration**: Tokens expire after 24 hours
2. **Check usage**: Tokens can only be used once
3. **Check database**: Verify token exists in `password_reset_tokens` table

### Frontend URL Issues

1. **Verify `FRONTEND_URL`**: Ensure it matches your frontend application URL
2. **Check for trailing slashes**: URLs should not have trailing slashes
3. **Production URLs**: Use HTTPS in production environments

## API Documentation

The password recovery endpoints are documented in Swagger UI, available at:
```
http://localhost:8080/swagger-ui.html
```

Navigate to the "Authentication" section to see detailed API documentation and try the endpoints interactively.
