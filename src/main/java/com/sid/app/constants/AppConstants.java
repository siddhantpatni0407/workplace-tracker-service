package com.sid.app.constants;

public class AppConstants {

    private AppConstants() {
    }

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

    // Tenant Management messages
    public static final String SUCCESS_TENANT_CREATED = "Tenant created successfully";
    public static final String SUCCESS_TENANT_UPDATED = "Tenant updated successfully";
    public static final String SUCCESS_TENANT_DELETED = "Tenant deleted successfully";
    public static final String SUCCESS_TENANT_STATUS_UPDATED = "Tenant status updated successfully";
    public static final String SUCCESS_TENANTS_RETRIEVED = "Tenants retrieved successfully";
    public static final String SUCCESS_TENANT_RETRIEVED = "Tenant retrieved successfully";
    public static final String SUCCESS_TENANT_STATS_RETRIEVED = "Tenant statistics retrieved successfully";
    public static final String SUCCESS_TENANT_USERS_RETRIEVED = "Tenant users retrieved successfully";
    public static final String ERROR_TENANT_NOT_FOUND = "Tenant not found";
    public static final String ERROR_TENANT_NAME_EXISTS = "Tenant name already exists";
    public static final String ERROR_TENANT_CODE_EXISTS = "Tenant code already exists";
    public static final String ERROR_INVALID_SUBSCRIPTION = "Invalid or inactive subscription";
    public static final String ERROR_TENANT_CREATION_FAILED = "Failed to create tenant";
    public static final String ERROR_TENANT_UPDATE_FAILED = "Failed to update tenant";

    // Analytics messages
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

    // Notes messages
    public static final String SUCCESS_NOTE_CREATED = "Note created successfully";
    public static final String SUCCESS_NOTE_UPDATED = "Note updated successfully";
    public static final String SUCCESS_NOTE_DELETED = "Note deleted successfully";
    public static final String SUCCESS_NOTE_RETRIEVED = "Note retrieved successfully";
    public static final String SUCCESS_NOTES_RETRIEVED = "Notes retrieved successfully";
    public static final String SUCCESS_NOTE_STATUS_UPDATED = "Note status updated successfully";
    public static final String SUCCESS_NOTE_PIN_TOGGLED = "Note pin status toggled successfully";
    public static final String SUCCESS_NOTE_COLOR_UPDATED = "Note color updated successfully";
    public static final String SUCCESS_NOTES_BULK_UPDATED = "Notes bulk updated successfully";
    public static final String SUCCESS_NOTES_BULK_DELETED = "Notes bulk deleted successfully";
    public static final String SUCCESS_NOTE_DUPLICATED = "Note duplicated successfully";
    public static final String SUCCESS_NOTE_STATS_RETRIEVED = "Note statistics retrieved successfully";
    public static final String ERROR_NOTE_NOT_FOUND = "Note not found";
    public static final String ERROR_NOTES_NOT_FOUND = "No notes found";
    public static final String ERROR_INVALID_NOTE_PARAMS = "Invalid note parameters";
    public static final String ERROR_NOTE_ACCESS_DENIED = "Access denied to note";

    /**
     * Default user.
     */
    public static final String DEFAULT_USER = "DEFAULT_USER";

    // Subscription Management messages
    public static final String SUCCESS_SUBSCRIPTIONS_RETRIEVED = "Subscriptions retrieved successfully";
    public static final String SUCCESS_ACTIVE_SUBSCRIPTIONS_RETRIEVED = "Active subscriptions retrieved successfully";
    public static final String SUCCESS_SUBSCRIPTION_RETRIEVED = "Subscription retrieved successfully";
    public static final String ERROR_SUBSCRIPTION_NOT_FOUND = "Subscription not found";
    public static final String ERROR_NO_SUBSCRIPTIONS_FOUND = "No subscriptions found";
    public static final String ERROR_INVALID_SUBSCRIPTION_CODE = "Invalid subscription code";

    // Platform User Authentication messages
    public static final String SUCCESS_PLATFORM_SIGNUP = "Platform user signup successful";
    public static final String SUCCESS_PLATFORM_LOGIN = "Platform user login successful";
    public static final String SUCCESS_PLATFORM_TOKEN_REFRESH = "Platform user token refresh successful";
    public static final String SUCCESS_PLATFORM_PROFILE_RETRIEVED = "Platform user profile retrieved successfully";
    public static final String ERROR_PLATFORM_SIGNUP_FAILED = "Signup failed due to server error";
    public static final String ERROR_PLATFORM_LOGIN_FAILED = "Login failed due to server error";
    public static final String ERROR_PLATFORM_TOKEN_REFRESH_FAILED = "Token refresh failed due to server error";
    public static final String ERROR_PLATFORM_PROFILE_NOT_FOUND = "Platform user profile not found";

    // Authentication and Registration Validation Messages
    public static final String ERROR_NAME_REQUIRED = "Name is required";
    public static final String ERROR_EMAIL_REQUIRED = "Email is required";
    public static final String ERROR_PASSWORD_REQUIRED = "Password is required";
    public static final String ERROR_ROLE_REQUIRED = "Role is required";
    public static final String ERROR_INVALID_EMAIL_FORMAT = "Invalid email format";
    public static final String ERROR_PASSWORD_MIN_LENGTH = "Password must be at least 8 characters long";
    public static final String ERROR_PLATFORM_USER_CODE_REQUIRED_SUPER_ADMIN = "Platform user code is required for SUPER_ADMIN role";
    public static final String ERROR_TENANT_CODE_REQUIRED_SUPER_ADMIN = "Tenant code is required for SUPER_ADMIN role";
    public static final String ERROR_TENANT_USER_CODE_REQUIRED_ADMIN = "Tenant user code is required for ADMIN role";
    public static final String ERROR_TENANT_USER_CODE_REQUIRED_USER_MANAGER = "Tenant user code is required for %s role";
    public static final String ERROR_INVALID_ROLE = "Invalid role: %s. Supported roles: SUPER_ADMIN, ADMIN, USER, MANAGER";

    // Authentication Messages
    public static final String ERROR_MISSING_REFRESH_TOKEN = "Missing refresh token.";
    public static final String ERROR_REFRESH_FAILED = "Refresh failed.";
    public static final String SUCCESS_PASSWORD_CHANGED = "Password changed successfully.";
    public static final String ERROR_USER_ID_REQUIRED = "userId is required. In production derive userId from auth token.";
    public static final String ERROR_FAILED_TO_CHANGE_PASSWORD = "Failed to change password.";

    // Tenant Management Operation Messages
    public static final String SUCCESS_TENANT_CREATED_MESSAGE = "Tenant created successfully";
    public static final String ERROR_TENANT_CREATION_FAILED_MESSAGE = "Failed to create tenant";
    public static final String SUCCESS_TENANTS_RETRIEVED_MESSAGE = "Tenants retrieved successfully";
    public static final String ERROR_TENANTS_FETCH_FAILED = "Failed to fetch tenants";
    public static final String SUCCESS_ACTIVE_TENANTS_RETRIEVED_MESSAGE = "Active tenants retrieved successfully";
    public static final String ERROR_ACTIVE_TENANTS_FETCH_FAILED = "Failed to fetch active tenants";
    public static final String SUCCESS_TENANT_RETRIEVED_MESSAGE = "Tenant retrieved successfully";
    public static final String ERROR_TENANT_FETCH_FAILED = "Failed to fetch tenant";
    public static final String SUCCESS_TENANT_UPDATED_MESSAGE = "Tenant updated successfully";
    public static final String ERROR_TENANT_UPDATE_FAILED_MESSAGE = "Failed to update tenant";
    public static final String SUCCESS_TENANT_STATUS_UPDATED_MESSAGE = "Tenant status updated successfully";
    public static final String ERROR_TENANT_STATUS_UPDATE_FAILED = "Failed to update tenant status";
    public static final String SUCCESS_TENANT_DELETED_MESSAGE = "Tenant deleted successfully";
    public static final String ERROR_TENANT_DELETE_FAILED = "Failed to delete tenant";
    public static final String SUCCESS_TENANTS_FOUND = "Tenants found successfully";
    public static final String ERROR_TENANTS_SEARCH_FAILED = "Failed to search tenants";
    public static final String SUCCESS_TENANT_STATS_RETRIEVED_MESSAGE = "Tenant statistics retrieved successfully";
    public static final String ERROR_TENANT_STATS_FETCH_FAILED = "Failed to fetch tenant statistics";
    public static final String SUCCESS_TENANT_USERS_RETRIEVED_MESSAGE = "Tenant users retrieved successfully";
    public static final String ERROR_TENANT_USERS_FETCH_FAILED = "Failed to fetch tenant users";

    // Tenant Management Info Messages
    public static final String INFO_NO_TENANTS_FOUND = "No tenants found";
    public static final String INFO_NO_ACTIVE_TENANTS_FOUND = "No active tenants found";
    public static final String INFO_NO_TENANTS_FOUND_FOR_SEARCH = "No tenants found for the given search term";
    public static final String INFO_NO_USERS_FOUND_FOR_TENANT = "No users found for this tenant";

    // Tenant Management Validation Messages
    public static final String ERROR_SEARCH_TERM_EMPTY = "Search term cannot be empty";

    // Common Sort Direction Constants
    public static final String SORT_DIRECTION_DESC = "desc";
    public static final String SORT_DIRECTION_ASC = "asc";

    // Platform Stats Messages
    public static final String SUCCESS_PLATFORM_STATS_RETRIEVED = "Platform statistics retrieved successfully.";
    public static final String ERROR_PLATFORM_STATS_FETCH_FAILED = "Failed to retrieve platform statistics";

    // Role codes
    public static final String ROLE_CODE_PLATFORM_USER = "PLATFORM_USER";
    public static final String ROLE_CODE_SUPER_ADMIN = "SUPER_ADMIN";
    public static final String ROLE_CODE_ADMIN = "ADMIN";
    public static final String ROLE_CODE_USER = "USER";
    public static final String ROLE_CODE_MANAGER = "MANAGER";
}
