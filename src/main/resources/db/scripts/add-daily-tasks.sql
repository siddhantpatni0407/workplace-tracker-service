-- =========================
-- Start of changeset : add-daily-tasks.sql
-- =========================

-- =========================
-- Create table: daily_tasks
-- =========================
CREATE TABLE IF NOT EXISTS daily_tasks (
    daily_task_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    daily_task_date DATE NOT NULL,
    daily_task_day VARCHAR(20), -- e.g., 'Monday', 'Tuesday', etc.
    task_number VARCHAR(20) NOT NULL, -- e.g., 'TSK-001'
    project_code VARCHAR(20), -- e.g., 'CRM', 'WPT'
    project_name VARCHAR(100), -- e.g., 'Customer Management', 'Workplace Tracker'
    story_task_bug_number VARCHAR(20), -- e.g., 'CRM-456', 'WPT-123'
    task_details TEXT,
    remarks TEXT,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_daily_tasks_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Add indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_daily_tasks_user_id ON daily_tasks(user_id);
CREATE INDEX IF NOT EXISTS idx_daily_tasks_date ON daily_tasks(daily_task_date);
CREATE INDEX IF NOT EXISTS idx_daily_tasks_project_code ON daily_tasks(project_code);

-- =========================
-- End of changeset : add-daily-tasks.sql
-- =========================
