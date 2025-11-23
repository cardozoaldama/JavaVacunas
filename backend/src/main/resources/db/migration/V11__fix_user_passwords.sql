-- Fix user passwords - correct BCrypt hash for 'admin123'
-- The previous hash in V10 was incorrect and did not match the password
-- This migration updates all test users with the correct BCrypt hash

-- Correct BCrypt hash for 'admin123' with cost factor 10
-- Hash: $2b$10$AmCyNSie0kDurcW5l32GiuJyCwIUH/Z3fuOHKD20hM2K7rN9B0a/y

UPDATE users
SET password_hash = '$2b$10$AmCyNSie0kDurcW5l32GiuJyCwIUH/Z3fuOHKD20hM2K7rN9B0a/y'
WHERE username IN ('admin', 'nurse', 'parent');

COMMIT;
