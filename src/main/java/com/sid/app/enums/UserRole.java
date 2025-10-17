package com.sid.app.enums;

/**
 * Enum representing different user roles in the system.
 * Each role has a code that corresponds to the role codes used in the database and authentication.
 *
 * <p>Author: Siddhant Patni</p>
 */
public enum UserRole {
    PLATFORM_USER("PLATFORM_USER"),
    SUPER_ADMIN("SUPER_ADMIN"),
    ADMIN("ADMIN"),
    USER("USER"),
    MANAGER("MANAGER");

    private final String code;

    UserRole(String code) {
        this.code = code;
    }

    /**
     * Get the role code as a string.
     *
     * @return the role code
     */
    public String getCode() {
        return code;
    }

    /**
     * Get UserRole enum from role code string.
     *
     * @param code the role code
     * @return the corresponding UserRole enum
     * @throws IllegalArgumentException if the role code is not found
     */
    public static UserRole fromCode(String code) {
        for (UserRole role : UserRole.values()) {
            if (role.getCode().equals(code)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Invalid role code: " + code);
    }

    @Override
    public String toString() {
        return code;
    }
}
