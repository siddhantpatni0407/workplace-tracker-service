-- =========================
-- Start of changeset : add-user_tasks-table.sql
-- =========================

-- =========================
-- Create table: user_tasks
-- =========================
CREATE TABLE IF NOT EXISTS user_tasks (
    user_task_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    task_title VARCHAR(500) NOT NULL,
    task_description TEXT,
    task_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED',
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    category VARCHAR(20) DEFAULT 'WORK',
    task_type VARCHAR(20) DEFAULT 'TASK',
    due_date DATE,
    reminder_date TIMESTAMP,
    tags TEXT[], -- PostgreSQL array of strings
    parent_task_id BIGINT,
    created_by BIGINT,
    remarks TEXT,
    is_recurring BOOLEAN DEFAULT FALSE,
    recurring_pattern VARCHAR(100),
    version INTEGER DEFAULT 1,
    access_count INTEGER DEFAULT 0,
    last_accessed_date TIMESTAMP,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_user_tasks_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_user_tasks_parent FOREIGN KEY (parent_task_id) REFERENCES user_tasks(user_task_id) ON DELETE CASCADE,
    CONSTRAINT fk_user_tasks_created_by FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE SET NULL,
    CONSTRAINT chk_task_status CHECK (status IN ('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED', 'ON_HOLD', 'CANCELLED')),
    CONSTRAINT chk_task_priority CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT')),
    CONSTRAINT chk_task_category CHECK (category IN ('WORK', 'PERSONAL', 'PROJECT', 'MEETING', 'LEARNING', 'ADMIN', 'OTHER')),
    CONSTRAINT chk_task_type CHECK (task_type IN ('TASK', 'SUBTASK', 'MILESTONE', 'BUG', 'FEATURE')),
    CONSTRAINT chk_due_date_after_task_date CHECK (due_date >= task_date OR due_date IS NULL),
    CONSTRAINT chk_version_positive CHECK (version > 0),
    CONSTRAINT chk_access_count_non_negative CHECK (access_count >= 0)
);

-- Create indexes for optimal performance
CREATE INDEX IF NOT EXISTS idx_user_tasks_user_id ON user_tasks(user_id);
CREATE INDEX IF NOT EXISTS idx_user_tasks_status ON user_tasks(status);
CREATE INDEX IF NOT EXISTS idx_user_tasks_priority ON user_tasks(priority);
CREATE INDEX IF NOT EXISTS idx_user_tasks_category ON user_tasks(category);
CREATE INDEX IF NOT EXISTS idx_user_tasks_task_type ON user_tasks(task_type);
CREATE INDEX IF NOT EXISTS idx_user_tasks_due_date ON user_tasks(due_date);
CREATE INDEX IF NOT EXISTS idx_user_tasks_task_date ON user_tasks(task_date);
CREATE INDEX IF NOT EXISTS idx_user_tasks_created_date ON user_tasks(created_date);
CREATE INDEX IF NOT EXISTS idx_user_tasks_modified_date ON user_tasks(modified_date);
CREATE INDEX IF NOT EXISTS idx_user_tasks_parent_id ON user_tasks(parent_task_id);
CREATE INDEX IF NOT EXISTS idx_user_tasks_created_by ON user_tasks(created_by);
CREATE INDEX IF NOT EXISTS idx_user_tasks_reminder_date ON user_tasks(reminder_date);

-- Create composite indexes for common queries
CREATE INDEX IF NOT EXISTS idx_user_tasks_user_status ON user_tasks(user_id, status);
CREATE INDEX IF NOT EXISTS idx_user_tasks_user_priority ON user_tasks(user_id, priority);
CREATE INDEX IF NOT EXISTS idx_user_tasks_user_due_date ON user_tasks(user_id, due_date);
CREATE INDEX IF NOT EXISTS idx_user_tasks_status_priority ON user_tasks(status, priority);

-- Create GIN index for tags array
CREATE INDEX IF NOT EXISTS idx_user_tasks_tags ON user_tasks USING GIN(tags);

-- Create partial indexes for specific use cases (removed CURRENT_DATE usage due to immutability requirements)
CREATE INDEX IF NOT EXISTS idx_user_tasks_active ON user_tasks(user_id, created_date)
WHERE status NOT IN ('COMPLETED', 'CANCELLED');

-- Create additional performance indexes
CREATE INDEX IF NOT EXISTS idx_user_tasks_due_date_status ON user_tasks(due_date, status)
WHERE due_date IS NOT NULL;
-- =========================
-- End of changeset : add-user_tasks-table.sql
-- =========================