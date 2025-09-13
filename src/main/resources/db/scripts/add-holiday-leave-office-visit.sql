-- =========================
-- Start of changeset : add-holiday-leave-office-visit.sql
-- =========================

-- =========================
-- Create table: holiday
-- =========================
CREATE TABLE IF NOT EXISTS holiday (
    holiday_id BIGSERIAL PRIMARY KEY,
    holiday_date DATE NOT NULL,
    name VARCHAR(100) NOT NULL,
    holiday_type VARCHAR(16) NOT NULL DEFAULT 'MANDATORY', -- MANDATORY or OPTIONAL
    description TEXT,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uq_holiday_date UNIQUE (holiday_date, name),
    CONSTRAINT chk_holiday_type CHECK (holiday_type IN ('MANDATORY','OPTIONAL'))
);

CREATE INDEX IF NOT EXISTS idx_holiday_date ON holiday(holiday_date);
CREATE INDEX IF NOT EXISTS idx_holiday_type ON holiday(holiday_type);

-- =========================
-- Create table: leave_policy (admin-managed master)
-- =========================
CREATE TABLE IF NOT EXISTS leave_policy (
    policy_id BIGSERIAL PRIMARY KEY,
    policy_code VARCHAR(50) NOT NULL UNIQUE, -- e.g. ANNUAL, SICK, CASUAL
    policy_name VARCHAR(100) NOT NULL,       -- human-friendly name
    default_annual_days INT NOT NULL DEFAULT 0,
    description TEXT,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_leave_policy_code ON leave_policy(policy_code);

-- Optional seed policies
-- INSERT INTO leave_policy (policy_code, policy_name, default_annual_days, description)
-- VALUES
--   ('ANNUAL','Annual Leave',18,'Standard annual leave allocation'),
--   ('SICK','Sick Leave',12,'Sick leave allocation'),
--   ('CASUAL','Casual Leave',6,'Casual leave allocation')
-- ON CONFLICT (policy_code) DO NOTHING;

-- =========================
-- Create table: user_leave
-- =========================
CREATE TABLE IF NOT EXISTS user_leave (
    user_leave_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    policy_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    days NUMERIC(6,2) NOT NULL, -- decimals: 0.5, 1.0, 1.5, etc.
    day_part VARCHAR(16), -- FULL, MORNING, AFTERNOON, CUSTOM
    notes TEXT,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_user_leave_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_user_leave_policy FOREIGN KEY (policy_id) REFERENCES leave_policy(policy_id) ON DELETE RESTRICT,
    CONSTRAINT chk_user_leave_day_part CHECK (day_part IS NULL OR day_part IN ('FULL','MORNING','AFTERNOON','CUSTOM')),
    CONSTRAINT chk_user_leave_halfday_days CHECK (day_part NOT IN ('MORNING','AFTERNOON') OR days = 0.5),
    CONSTRAINT chk_user_leave_fullday_days CHECK (day_part IS DISTINCT FROM 'FULL' OR days >= 1.0)
);

CREATE INDEX IF NOT EXISTS idx_user_leave_user ON user_leave(user_id);
CREATE INDEX IF NOT EXISTS idx_user_leave_policy ON user_leave(policy_id);
CREATE INDEX IF NOT EXISTS idx_user_leave_period ON user_leave(start_date, end_date);
CREATE INDEX IF NOT EXISTS idx_user_leave_day_part ON user_leave(day_part);

-- =========================
-- Create table: user_leave_balance
-- =========================
CREATE TABLE IF NOT EXISTS user_leave_balance (
    user_leave_balance_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    policy_id BIGINT NOT NULL,
    year INT NOT NULL,                      -- calendar year
    allocated_days NUMERIC(6,2) NOT NULL,   -- from leave_policy.default_annual_days or admin allocation
    used_days NUMERIC(6,2) NOT NULL DEFAULT 0,
    remaining_days NUMERIC(6,2) NOT NULL,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_ulb_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_ulb_policy FOREIGN KEY (policy_id) REFERENCES leave_policy(policy_id) ON DELETE RESTRICT,
    CONSTRAINT uq_ulb_user_policy_year UNIQUE (user_id, policy_id, year)
);

CREATE INDEX IF NOT EXISTS idx_ulb_user ON user_leave_balance(user_id);
CREATE INDEX IF NOT EXISTS idx_ulb_policy ON user_leave_balance(policy_id);
CREATE INDEX IF NOT EXISTS idx_ulb_year ON user_leave_balance(year);

-- =========================
-- Create table: office_visit
-- =========================
CREATE TABLE IF NOT EXISTS office_visit (
    office_visit_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    visit_date DATE NOT NULL,
    day_of_week INT NOT NULL, -- 1=Monday .. 7=Sunday
    visit_type VARCHAR(32) NOT NULL, -- WFO, WFH, HYBRID, OTHERS
    notes TEXT,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_office_visit_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT uq_user_visit_date UNIQUE (user_id, visit_date)
);

CREATE INDEX IF NOT EXISTS idx_office_visit_user_date ON office_visit(user_id, visit_date);
CREATE INDEX IF NOT EXISTS idx_office_visit_visit_type ON office_visit(visit_type);

-- =========================
-- End of changeset : add-holiday-leave-office-visit.sql
-- =========================