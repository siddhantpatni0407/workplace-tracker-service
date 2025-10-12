package com.sid.app.enums;

/**
 * Enumeration for task categories
 * Used to organize and classify tasks by domain
 */
public enum TaskCategory {
    WORK("Work"),
    PERSONAL("Personal"),
    PROJECT("Project"),
    MEETING("Meeting"),
    LEARNING("Learning"),
    ADMIN("Admin"),
    OTHER("Other");

    private final String displayName;

    TaskCategory(String displayName) {
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
