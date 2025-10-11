-- =========================
-- Start of changeset : add-user-notes-table.sql
-- =========================

-- =========================
-- Create table: user_notes
-- =========================
CREATE TABLE IF NOT EXISTS user_notes (
    user_note_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    note_title VARCHAR(500) NOT NULL,
    note_content TEXT NOT NULL,
    note_type VARCHAR(20) NOT NULL DEFAULT 'TEXT',
    color VARCHAR(20) NOT NULL DEFAULT 'DEFAULT',
    category VARCHAR(20) NOT NULL DEFAULT 'PERSONAL',
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    is_pinned BOOLEAN NOT NULL DEFAULT FALSE,
    is_shared BOOLEAN NOT NULL DEFAULT FALSE,
    reminder_date TIMESTAMP NULL,
    version INT NOT NULL DEFAULT 1,
    access_count INT NOT NULL DEFAULT 0,
    last_accessed_date TIMESTAMP NULL,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_user_notes_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT chk_note_type CHECK (note_type IN ('TEXT', 'CHECKLIST', 'DRAWING', 'VOICE', 'IMAGE', 'LINK')),
    CONSTRAINT chk_color CHECK (color IN ('DEFAULT', 'YELLOW', 'ORANGE', 'RED', 'PINK', 'PURPLE', 'BLUE', 'TEAL', 'GREEN', 'BROWN', 'GREY')),
    CONSTRAINT chk_category CHECK (category IN ('PERSONAL', 'WORK', 'IDEAS', 'REMINDERS', 'PROJECTS', 'MEETING_NOTES', 'SHOPPING', 'TRAVEL', 'HEALTH', 'FINANCE', 'LEARNING', 'INSPIRATION')),
    CONSTRAINT chk_priority CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT')),
    CONSTRAINT chk_status CHECK (status IN ('ACTIVE', 'ARCHIVED', 'DELETED', 'PINNED'))
);

CREATE INDEX IF NOT EXISTS idx_user_notes_user_id ON user_notes(user_id);
CREATE INDEX IF NOT EXISTS idx_user_notes_note_type ON user_notes(note_type);
CREATE INDEX IF NOT EXISTS idx_user_notes_category ON user_notes(category);
CREATE INDEX IF NOT EXISTS idx_user_notes_status ON user_notes(status);
CREATE INDEX IF NOT EXISTS idx_user_notes_is_pinned ON user_notes(is_pinned);
CREATE INDEX IF NOT EXISTS idx_user_notes_created_date ON user_notes(created_date);
CREATE INDEX IF NOT EXISTS idx_user_notes_modified_date ON user_notes(modified_date);

-- =========================
-- End of changeset : add-user-notes-table.sql
-- =========================
