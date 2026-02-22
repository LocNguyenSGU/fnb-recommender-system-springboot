-- Add is_verified column to users table
ALTER TABLE users ADD COLUMN is_verified BOOLEAN DEFAULT FALSE;

-- Set existing users with email_verified_at as verified
UPDATE users SET is_verified = TRUE WHERE email_verified_at IS NOT NULL;
