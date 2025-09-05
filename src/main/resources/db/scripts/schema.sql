-- Create Table: users
CREATE TABLE IF NOT EXISTS users (
    user_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    mobile_number VARCHAR(15) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    password_encryption_key_version INT NOT NULL,
    role VARCHAR(50) NOT NULL,
    last_login_time TIMESTAMP WITH TIME ZONE,
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    login_attempts INT DEFAULT 0 NOT NULL,
    account_locked BOOLEAN DEFAULT FALSE NOT NULL,
    created_date TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    modified_date TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Create Table: encryption_keys
CREATE TABLE encryption_keys (
    encryption_keys_id BIGSERIAL PRIMARY KEY,
    key_version INT UNIQUE NOT NULL,
    secret_key VARCHAR(255) UNIQUE NOT NULL,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);