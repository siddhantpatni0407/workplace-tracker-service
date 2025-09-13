-- =========================
-- Start of changeset : schema.sql
-- =========================

-- =========================
-- Create user_role table
-- =========================
CREATE TABLE IF NOT EXISTS user_role (
    role_id BIGSERIAL PRIMARY KEY,
    role VARCHAR(15) NOT NULL UNIQUE,  -- e.g. 'USER', 'ADMIN'
    description TEXT,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Seed default roles (idempotent)
INSERT INTO user_role (role, description)
VALUES ('USER', 'Application User')
ON CONFLICT (role) DO NOTHING;

INSERT INTO user_role (role, description)
VALUES ('ADMIN', 'Administrator')
ON CONFLICT (role) DO NOTHING;

-- =========================
-- Create users table
-- =========================
CREATE TABLE IF NOT EXISTS users (
    user_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    email VARCHAR(30) UNIQUE NOT NULL,
    mobile_number VARCHAR(15) UNIQUE NOT NULL,
    password VARCHAR(50) NOT NULL,
    password_encryption_key_version INT NOT NULL,
    role_id BIGINT NOT NULL,
    last_login_time TIMESTAMP WITH TIME ZONE,
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    login_attempts INT DEFAULT 0 NOT NULL,
    account_locked BOOLEAN DEFAULT FALSE NOT NULL,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES user_role(role_id)
);

-- =========================
-- Create encryption_keys table
-- =========================
CREATE TABLE IF NOT EXISTS encryption_keys (
    encryption_key_id BIGSERIAL PRIMARY KEY,
    key_version INT UNIQUE NOT NULL,
    secret_key VARCHAR(255) UNIQUE NOT NULL,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- =========================
-- Indexes
-- =========================
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_mobile ON users(mobile_number);
CREATE INDEX IF NOT EXISTS idx_user_role_role ON user_role(role);

-- =========================
-- Create table: user_settings
-- =========================
CREATE TABLE IF NOT EXISTS user_settings (
    user_setting_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    timezone VARCHAR(64) DEFAULT 'UTC',
    work_week_start INT DEFAULT 1, -- 1 = Monday .. 7 = Sunday
    language VARCHAR(16) DEFAULT 'English',
    date_format VARCHAR(32) DEFAULT 'yyyy-MM-dd',
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_user_settings_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Index to speed up lookups by user_id (user_id is UNIQUE so this is optional but explicit)
CREATE INDEX IF NOT EXISTS idx_user_settings_user ON user_settings(user_id);

-- =========================
-- End of changeset : schema.sql
-- =========================