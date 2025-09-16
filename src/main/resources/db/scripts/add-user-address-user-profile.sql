-- =========================
-- Create table: user_profile
-- =========================
CREATE TABLE IF NOT EXISTS user_profile (
    user_profile_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE, -- one-to-one with users
    date_of_birth DATE,
    gender VARCHAR(32),
    department VARCHAR(100),
    position VARCHAR(100),
    employee_id VARCHAR(50),
    date_of_joining DATE,
    profile_picture TEXT,
    bio VARCHAR(500),
    emergency_contact_name VARCHAR(100),
    emergency_contact_phone VARCHAR(20),
    emergency_contact_relation VARCHAR(50),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_user_profile_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Index for lookups
CREATE INDEX IF NOT EXISTS idx_user_profile_user_id ON user_profile(user_id);

-- =========================
-- Create table: user_address (for addresses)
-- =========================
CREATE TABLE IF NOT EXISTS user_address (
    user_address_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL, -- FK to users
    address TEXT,
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100),
    postal_code VARCHAR(30),
    is_primary BOOLEAN DEFAULT TRUE NOT NULL,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_user_address_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_user_address_user_id ON user_address(user_id);

-- one primary address per user
CREATE UNIQUE INDEX IF NOT EXISTS ux_user_address_user_primary
    ON user_address(user_id)
    WHERE is_primary = TRUE;