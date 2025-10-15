-- =========================
-- Start of changeset : schema_new.sql
-- =========================

-- =========================
-- Create user_role table
-- =========================
CREATE TABLE IF NOT EXISTS user_role (
    role_id BIGSERIAL PRIMARY KEY,
    role VARCHAR(15) NOT NULL UNIQUE,  -- e.g. 'USER', 'ADMIN', 'SUPER_ADMIN', 'PLATFORM_USER', 'MANAGER'
    description TEXT,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Seed default roles (idempotent)
INSERT INTO user_role (role, description)
VALUES
('PLATFORM_USER', 'Platform-level root user')
ON CONFLICT (role) DO NOTHING;

INSERT INTO user_role (role, description)
VALUES
('SUPER_ADMIN', 'Tenant-level Super Administrator')
ON CONFLICT (role) DO NOTHING;

INSERT INTO user_role (role, description)
VALUES
('ADMIN', 'Tenant-level Administrator')
ON CONFLICT (role) DO NOTHING;

INSERT INTO user_role (role, description)
VALUES
('USER', 'Application User')
ON CONFLICT (role) DO NOTHING;

INSERT INTO user_role (role, description)
VALUES
('MANAGER', 'User with Manager privileges')
ON CONFLICT (role) DO NOTHING;

-- Index for role lookup
CREATE INDEX IF NOT EXISTS idx_user_role_role ON user_role(role);


-- =========================
-- Platform users table
-- =========================
CREATE TABLE IF NOT EXISTS platform_user (
    platform_user_id BIGSERIAL PRIMARY KEY,
    role_id BIGINT NOT NULL,  -- PLATFORM_USER
    platform_user_code VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    mobile_number VARCHAR(20) UNIQUE,
    password VARCHAR(255) NOT NULL,
    password_encryption_key_version INT DEFAULT 1,
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    last_login_time TIMESTAMP WITH TIME ZONE,
    login_attempts INT DEFAULT 0 NOT NULL,
    account_locked BOOLEAN DEFAULT FALSE NOT NULL,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_platform_user_role FOREIGN KEY (role_id) REFERENCES user_role(role_id)
);

CREATE INDEX IF NOT EXISTS idx_platform_user_email ON platform_user(email);
CREATE INDEX IF NOT EXISTS idx_platform_user_mobile ON platform_user(mobile_number);
CREATE INDEX IF NOT EXISTS idx_platform_user_role_id ON platform_user(role_id);

CREATE INDEX IF NOT EXISTS idx_platform_user_code ON platform_user(platform_user_code);

-- =========================
-- App subscription table
-- =========================
CREATE TABLE IF NOT EXISTS app_subscription (
    app_subscription_id BIGSERIAL PRIMARY KEY,
    subscription_code VARCHAR(50) UNIQUE NOT NULL,
    subscription_name VARCHAR(100) NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

INSERT INTO app_subscription (subscription_code, subscription_name, description, is_active)
VALUES
('BASIC', 'Basic Plan', 'Default basic plan with standard access', TRUE),
('LITE', 'Lite Plan', 'Lite plan with limited features', TRUE),
('PRO', 'Pro Plan', 'Pro plan with full features', TRUE)
ON CONFLICT (subscription_code) DO NOTHING;

CREATE INDEX IF NOT EXISTS idx_app_subscription_code ON app_subscription(subscription_code);


-- =========================
-- Tenant table
-- =========================
CREATE TABLE IF NOT EXISTS tenant (
    tenant_id BIGSERIAL PRIMARY KEY,
    tenant_name VARCHAR(150) UNIQUE NOT NULL,
    tenant_code VARCHAR(20) UNIQUE NOT NULL,  -- unique code for tenant, e.g., "ACME001"
    app_subscription_id BIGINT NOT NULL DEFAULT 1,  -- default BASIC
    contact_email VARCHAR(150),
    contact_phone VARCHAR(20),
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    subscription_start_date TIMESTAMP,
    subscription_end_date TIMESTAMP,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_tenant_subscription FOREIGN KEY (app_subscription_id) REFERENCES app_subscription(app_subscription_id)
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_tenant_name ON tenant(tenant_name);
CREATE INDEX IF NOT EXISTS idx_tenant_subscription_id ON tenant(app_subscription_id);
CREATE INDEX IF NOT EXISTS idx_tenant_code ON tenant(tenant_code);

-- =========================
-- Tenant users table (SUPER_ADMIN + ADMIN)
-- =========================
CREATE TABLE IF NOT EXISTS tenant_user (
    tenant_user_id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    platform_user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,           -- SUPER_ADMIN or ADMIN
    tenant_user_code VARCHAR(20) UNIQUE NOT NULL,
    manager_tenant_user_id BIGINT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    mobile_number VARCHAR(20) UNIQUE,
    password VARCHAR(255) NOT NULL,
    password_encryption_key_version INT DEFAULT 1,
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    last_login_time TIMESTAMP WITH TIME ZONE,
    login_attempts INT DEFAULT 0 NOT NULL,
    account_locked BOOLEAN DEFAULT FALSE NOT NULL,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_tenant_user_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(tenant_id),
    CONSTRAINT fk_tenant_user_platform_user FOREIGN KEY (platform_user_id) REFERENCES platform_user(platform_user_id),
    CONSTRAINT fk_tenant_user_role FOREIGN KEY (role_id) REFERENCES user_role(role_id),
    CONSTRAINT fk_tenant_user_manager FOREIGN KEY (manager_tenant_user_id) REFERENCES tenant_user(tenant_user_id)
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_tenant_super_admin ON tenant_user(tenant_id, role_id) WHERE role_id = 2;  -- adjust SUPER_ADMIN role_id
CREATE INDEX IF NOT EXISTS idx_tenant_user_tenant_id ON tenant_user(tenant_id);
CREATE INDEX IF NOT EXISTS idx_tenant_user_platform_user_id ON tenant_user(platform_user_id);
CREATE INDEX IF NOT EXISTS idx_tenant_user_role_id ON tenant_user(role_id);
CREATE INDEX IF NOT EXISTS idx_tenant_user_email ON tenant_user(email);
CREATE INDEX IF NOT EXISTS idx_tenant_user_mobile ON tenant_user(mobile_number);

CREATE INDEX IF NOT EXISTS idx_tenant_user_code ON tenant_user(tenant_user_code);

-- =========================
-- Users table (mapped to tenant_user)
-- =========================
CREATE TABLE IF NOT EXISTS users (
    user_id BIGSERIAL PRIMARY KEY,
    tenant_user_id BIGINT NOT NULL, -- reference to tenant_user
    name VARCHAR(50) NOT NULL,
    email VARCHAR(30) UNIQUE NOT NULL,
    mobile_number VARCHAR(15) UNIQUE NOT NULL,
    password VARCHAR(50) NOT NULL,
    password_encryption_key_version INT NOT NULL,
    role_id BIGINT NOT NULL, -- reference to user_role
    last_login_time TIMESTAMP WITH TIME ZONE,
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    login_attempts INT DEFAULT 0 NOT NULL,
    account_locked BOOLEAN DEFAULT FALSE NOT NULL,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES user_role(role_id),
    CONSTRAINT fk_users_tenant_user FOREIGN KEY (tenant_user_id) REFERENCES tenant_user(tenant_user_id)
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_mobile ON users(mobile_number);
CREATE INDEX IF NOT EXISTS idx_users_tenant_user_id ON users(tenant_user_id);


-- =========================
-- Role-permission table
-- =========================
CREATE TABLE IF NOT EXISTS role_permission (
    role_permission_id BIGSERIAL PRIMARY KEY,
    role_id BIGINT NOT NULL UNIQUE,
    permission_names TEXT[] NOT NULL, -- array of permission names
    description TEXT,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_rp_role FOREIGN KEY (role_id) REFERENCES user_role(role_id)
);

CREATE INDEX IF NOT EXISTS idx_role_permission_role_id ON role_permission(role_id);


-- =========================
-- User-role mapping table
-- =========================
CREATE TABLE IF NOT EXISTS user_role_mapping (
    user_role_mapping_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_urm_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_urm_role FOREIGN KEY (role_id) REFERENCES user_role(role_id),
    UNIQUE(user_id, role_id)
);

CREATE INDEX IF NOT EXISTS idx_urm_user_id ON user_role_mapping(user_id);
CREATE INDEX IF NOT EXISTS idx_urm_role_id ON user_role_mapping(role_id);


-- =========================
-- Encryption keys table
-- =========================
CREATE TABLE IF NOT EXISTS encryption_keys (
    encryption_key_id BIGSERIAL PRIMARY KEY,
    key_version INT UNIQUE NOT NULL,
    secret_key VARCHAR(255) UNIQUE NOT NULL,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);


-- =========================
-- User settings table
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

CREATE INDEX IF NOT EXISTS idx_user_settings_user ON user_settings(user_id);

-- =========================
-- End of changeset : schema_new.sql
-- =========================
