package com.sid.app.constants;

public class AppConstants {

    private AppConstants() {
    }

    // Auth endpoints
    public static final String USER_REGISTER_ENDPOINT = "/api/v1/workplace-tracker-service/register";
    public static final String USER_LOGIN_ENDPOINT = "/api/v1/workplace-tracker-service/login";
    public static final String FORGOT_PASSWORD_RESET_ENDPOINT = "/api/v1/workplace-tracker-service/forgot/reset";
    public static final String USER_CHANGE_PASSWORD_ENDPOINT = "/api/v1/workplace-tracker-service/user/change-password";

    // User endpoints
    public static final String FETCH_ALL_USERS_ENDPOINT = "/api/v1/workplace-tracker-service/user/fetch";
    public static final String USER_ENDPOINT = "/api/v1/workplace-tracker-service/user";
    public static final String USER_STATUS_ENDPOINT = "/api/v1/workplace-tracker-service/user/status";
    public static final String USER_SETTINGS_ENDPOINT = "/api/v1/workplace-tracker-service/user/settings";
    public static final String USER_PROFILE_ENDPOINT = "/api/v1/workplace-tracker-service/user/profile";;

    // DB endpoints
    public static final String DB_BACKUP_ENDPOINT = "/api/v1/workplace-tracker-service/db-backup";

    // Auth refresh
    public static final String AUTH_REFRESH_ENDPOINT = "/api/v1/workplace-tracker-service/auth/refresh";

    // Special Days endpoints
    public static final String SPECIAL_DAYS_ENDPOINT = "/api/v1/workplace-tracker-service/special-days";
    public static final String SPECIAL_DAYS_CURRENT_MONTH_ENDPOINT = "/api/v1/workplace-tracker-service/special-days/current-month";
    public static final String SPECIAL_DAYS_BIRTHDAYS_ENDPOINT = "/api/v1/workplace-tracker-service/special-days/birthdays";
    public static final String SPECIAL_DAYS_ANNIVERSARIES_ENDPOINT = "/api/v1/workplace-tracker-service/special-days/anniversaries";

    // Leave / holiday / visits endpoints (full paths, no base concatenation)
    public static final String LEAVE_POLICY_ENDPOINT = "/api/v1/workplace-tracker-service/leave-policies";
    public static final String EXACT_LEAVE_POLICY_ENDPOINT = "/api/v1/workplace-tracker-service/leave-policies/exact";
    public static final String USER_LEAVE_ENDPOINT = "/api/v1/workplace-tracker-service/user-leaves";
    public static final String USER_LEAVE_BALANCE_ENDPOINT = "/api/v1/workplace-tracker-service/user-leave-balance";
    public static final String USER_LEAVE_BALANCE_ADJUST_ENDPOINT = "/api/v1/workplace-tracker-service/user-leave-balance/adjust";
    public static final String USER_LEAVE_BALANCE_RECALCULATE_ENDPOINT = "/api/v1/workplace-tracker-service/user-leave-balance/recalculate";

    public static final String HOLIDAYS_ENDPOINT = "/api/v1/workplace-tracker-service/holidays";
    public static final String VISITS_ENDPOINT = "/api/v1/workplace-tracker-service/visits";
    public static final String FETCH_DAILY_VIEW_ENDPOINT = "/api/v1/workplace-tracker-service/fetch-daily-view-records";
    public static final String ANALYTICS_VISITS_LEAVES_AGG_ENDPOINT = "/api/v1/workplace-tracker-service/analytics/visits-leaves-aggregate";

    // Holidays messages
    public static final String SUCCESS_HOLIDAYS_RETRIEVED = "Holidays retrieved";
    public static final String ERROR_NO_HOLIDAYS_FOUND = "No holidays found";
    public static final String ERROR_NO_HOLIDAYS_IN_RANGE = "No holidays found in range";
    public static final String ERROR_INVALID_DATE_RANGE = "Invalid date format for from/to. Expected yyyy-MM-dd.";
    // (existing ones you already have)
    public static final String SUCCESS_HOLIDAY_CREATED = "Holiday created";
    public static final String SUCCESS_HOLIDAY_UPDATED = "Holiday updated";
    public static final String SUCCESS_HOLIDAY_DELETED = "Holiday deleted";

    // Leave policy messages & errors
    public static final String ERROR_NO_LEAVE_POLICIES_FOUND = "No leave policies found";
    public static final String ERROR_INVALID_POLICY_ID = "policyId must be provided and greater than 0";
    public static final String ERROR_INTERNAL_SERVER = "Internal server error";

    // Visits messages & errors
    public static final String SUCCESS_VISITS_RETRIEVED = "Visits retrieved";
    public static final String SUCCESS_VISIT_SAVED = "Visit saved successfully";
    public static final String SUCCESS_VISIT_DELETED = "Visit deleted successfully";
    public static final String ERROR_NO_VISITS_FOUND = "No visits found";
    public static final String ERROR_INVALID_VISIT_PARAMS = "Invalid parameters: userId must be > 0 and month between 1-12";
    public static final String ERROR_INVALID_VISIT_ID = "Invalid officeVisitId";

    // Leave balance messages & errors
    public static final String SUCCESS_BALANCE_RETRIEVED = "User leave balance retrieved successfully";
    public static final String SUCCESS_BALANCE_UPSERTED = "User leave balance created/updated successfully";

    // User leave messages & errors
    public static final String SUCCESS_LEAVES_RETRIEVED = "User leaves retrieved";
    public static final String ERROR_NO_LEAVES_FOR_USER = "No leaves found for user";
    public static final String ERROR_INVALID_USER_ID = "Invalid userId";
    public static final String ERROR_INVALID_LEAVE_ID = "Invalid userLeaveId";

    // Daily View Messages
    public static final String SUCCESS_DAILY_VIEW_RETRIEVED = "Daily view records retrieved";
    public static final String ERROR_INVALID_DAILY_VIEW_PARAMS = "Invalid parameters. Provide userId and either (year & month) or (from & to)";
    public static final String ERROR_NO_DAILY_VIEW_FOUND = "No daily view records found";
    public static final String ERROR_DATE_RANGE_TOO_LARGE = "Requested date range is too large. Max allowed days = 366";

    // Messages for responses
    public static final String SUCCESS_POLICY_RETRIEVED = "Policy retrieved";
    public static final String SUCCESS_POLICY_CREATED = "Policy created";
    public static final String SUCCESS_POLICY_UPDATED = "Policy updated";
    public static final String SUCCESS_LEAVE_CREATED = "Leave created";
    public static final String SUCCESS_LEAVE_UPDATED = "Leave updated";
    public static final String SUCCESS_LEAVE_DELETED = "Leave deleted";

    // Status
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILED = "FAILED";

    // Common Messages
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
    public static final String SUCCESS_ANALYTICS_RETRIEVED = "Analytics aggregated data retrieved";
    public static final String ERROR_INVALID_ANALYTICS_PARAMS = "Invalid analytics parameters. Provide from, to and groupBy(month|year|week)";
    public static final String SUCCESS_BALANCE_ADJUSTED = "Balance adjusted";
    public static final String SUCCESS_BALANCE_RECALCULATED = "Balance recalculated";
    public static final String ERROR_INVALID_BALANCE_PARAMS = "Invalid parameters for balance API";

    // User settings messages
    public static final String SUCCESS_MESSAGE_USER_SETTINGS_RETRIEVED = "User settings retrieved.";
    public static final String SUCCESS_MESSAGE_USER_SETTINGS_SAVED = "User settings saved.";
    public static final String SUCCESS_MESSAGE_USER_SETTINGS_DELETED = "User settings deleted.";
    public static final String ERROR_MESSAGE_RETRIEVE_USER_SETTINGS = "Unable to retrieve user settings.";
    public static final String ERROR_MESSAGE_SAVE_USER_SETTINGS = "Unable to save user settings.";
    public static final String ERROR_MESSAGE_DELETE_USER_SETTINGS = "Unable to delete user settings.";

    // Special Days messages
    public static final String SUCCESS_SPECIAL_DAYS_RETRIEVED = "Special Day Data retrieved successfully";
    public static final String SUCCESS_CURRENT_MONTH_SPECIAL_DAYS_RETRIEVED = "Current month special days retrieved successfully";
    public static final String SUCCESS_BIRTHDAYS_RETRIEVED = "Birthdays retrieved successfully";
    public static final String SUCCESS_ANNIVERSARIES_RETRIEVED = "Work anniversaries retrieved successfully";
    public static final String ERROR_SPECIAL_DAYS_RETRIEVAL_FAILED = "Failed to retrieve special days data";
    public static final String ERROR_CURRENT_MONTH_SPECIAL_DAYS_FAILED = "Failed to retrieve current month special days";

    /**
     * Default user.
     */
    public static final String DEFAULT_USER = "DEFAULT_USER";
}
