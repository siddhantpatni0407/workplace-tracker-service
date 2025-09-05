package com.sid.app.constants;

public class AppConstants {

    private AppConstants() {
    }

    // Auth endpoints
    public static final String USER_REGISTER_ENDPOINT = "/api/v1/workplace-tracker-service/register";
    public static final String USER_LOGIN_ENDPOINT = "/api/v1/workplace-tracker-service/login";
    public static final String FORGOT_PASSWORD_RESET_ENDPOINT = "/api/v1/workplace-tracker-service/forgot/reset";

    public static final String DB_BACKUP_ENDPOINT = "/api/v1/workplace-tracker-service/db-backup";
    public static final String FETCH_ALL_USERS_ENDPOINT = "/api/v1/workplace-tracker-service/user/fetch";
    public static final String USER_ENDPOINT = "/api/v1/workplace-tracker-service/user";

    // Status
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILED = "FAILED";

    // Messages
    public static final String SUCCESS_MESSAGE_REGISTRATION_SUCCESSFUL = "User registered successfully.";
    public static final String LOGIN_SUCCESSFUL_MESSAGE = "Login successful.";
    public static final String ERROR_MESSAGE_EMAIL_EXISTS = "Email already exists.";
    public static final String ERROR_MESSAGE_MOBILE_EXISTS = "Mobile number already exists.";
    public static final String ERROR_MESSAGE_USER_NOT_FOUND = "User not found.";
    public static final String ERROR_MESSAGE_INVALID_LOGIN = "Invalid credentials.";
    public static final String ERROR_MESSAGE_ACCOUNT_LOCKED = "Account is locked.";
    public static final String ERROR_MESSAGE_INACTIVE_ACCOUNT = "Account is inactive.";
    public static final String ERROR_MESSAGE_REGISTRATION = "Registration failed.";
    public static final String ERROR_MESSAGE_LOGIN = "Login failed.";

    /**
     * Default user.
     */
    public static final String DEFAULT_USER = "DEFAULT_USER";
}
