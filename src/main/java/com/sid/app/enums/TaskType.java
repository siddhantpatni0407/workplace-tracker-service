package com.sid.app.enums;

/**
 * Enumeration for task types
 * Defines the nature and classification of different task types
 */
public enum TaskType {
    TASK("Task"),
    SUBTASK("Subtask"),
    MILESTONE("Milestone"),
    BUG("Bug"),
    FEATURE("Feature");

    private final String displayName;

    TaskType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
