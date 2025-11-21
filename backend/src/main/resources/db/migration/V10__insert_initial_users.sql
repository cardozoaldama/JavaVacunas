-- Insert initial users for testing
-- Note: Passwords are BCrypt hashed with cost factor 10
-- Default password for all users: admin123

-- Admin user (DOCTOR role)
INSERT INTO users (username, password_hash, first_name, last_name, email, role, license_number, is_active)
VALUES (
    'admin',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',  -- admin123
    'Admin',
    'Sistema',
    'admin@javacunas.gov.py',
    'DOCTOR',
    'DOC-001',
    'Y'
);

-- Nurse user
INSERT INTO users (username, password_hash, first_name, last_name, email, role, license_number, is_active)
VALUES (
    'nurse',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',  -- admin123
    'Enfermera',
    'Principal',
    'nurse@javacunas.gov.py',
    'NURSE',
    'NUR-001',
    'Y'
);

-- Parent user
INSERT INTO users (username, password_hash, first_name, last_name, email, role, is_active)
VALUES (
    'parent',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',  -- admin123
    'Padre',
    'Ejemplo',
    'parent@example.com',
    'PARENT',
    'Y'
);

COMMIT;
