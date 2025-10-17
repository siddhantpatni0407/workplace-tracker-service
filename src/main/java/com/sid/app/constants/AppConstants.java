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
    public static final String USER_PROFILE_ENDPOINT = "/api/v1/workplace-tracker-service/user/profile";
    public static final String USERS_BY_TENANT_ENDPOINT = "/api/v1/workplace-tracker-service/user/by-tenant";
    public static final String ACTIVE_USERS_BY_TENANT_ENDPOINT = "/api/v1/workplace-tracker-service/user/active-by-tenant";
    public static final String SEARCH_USERS_BY_TENANT_ENDPOINT = "/api/v1/workplace-tracker-service/user/search-by-tenant";

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

    // Daily Tasks endpoints
    public static final String DAILY_TASKS_ENDPOINT = "/api/v1/workplace-tracker-service/daily-tasks";
    public static final String USER_DAILY_TASKS_ENDPOINT = "/api/v1/workplace-tracker-service/daily-tasks/user";
    public static final String USER_DAILY_TASKS_DATE_RANGE_ENDPOINT = "/api/v1/workplace-tracker-service/daily-tasks/user/date-range";
    public static final String USER_DAILY_TASKS_DATE_ENDPOINT = "/api/v1/workplace-tracker-service/daily-tasks/user/date";

    // UserNotes endpoints
    public static final String NOTES_ENDPOINT = "/api/v1/workplace-tracker-service/notes";
    public static final String NOTES_USER_ENDPOINT = "/api/v1/workplace-tracker-service/notes/user";
    public static final String NOTES_BY_TYPE_ENDPOINT = "/api/v1/workplace-tracker-service/notes/by-type";
    public static final String NOTES_BY_CATEGORY_ENDPOINT = "/api/v1/workplace-tracker-service/notes/by-category";
    public static final String NOTES_PINNED_ENDPOINT = "/api/v1/workplace-tracker-service/notes/pinned";
    public static final String NOTES_ARCHIVED_ENDPOINT = "/api/v1/workplace-tracker-service/notes/archived";
    public static final String NOTES_SEARCH_ENDPOINT = "/api/v1/workplace-tracker-service/notes/search";
    public static final String NOTES_STATS_ENDPOINT = "/api/v1/workplace-tracker-service/notes/stats";
    public static final String NOTES_BULK_UPDATE_ENDPOINT = "/api/v1/workplace-tracker-service/notes/bulk-update";
    public static final String NOTES_BULK_DELETE_ENDPOINT = "/api/v1/workplace-tracker-service/notes/bulk-delete";
    public static final String NOTES_STATUS_ENDPOINT = "/api/v1/workplace-tracker-service/notes/status";
    public static final String NOTES_PIN_ENDPOINT = "/api/v1/workplace-tracker-service/notes/pin";
    public static final String NOTES_COLOR_ENDPOINT = "/api/v1/workplace-tracker-service/notes/color";
    public static final String NOTES_DUPLICATE_ENDPOINT = "/api/v1/workplace-tracker-service/notes/duplicate";

    // UserTasks endpoints
    public static final String TASKS_ENDPOINT = "/api/v1/workplace-tracker-service/tasks";
    public static final String TASKS_USER_ENDPOINT = "/api/v1/workplace-tracker-service/tasks/user";
    public static final String TASKS_DETAILS_ENDPOINT = "/api/v1/workplace-tracker-service/tasks/details";
    public static final String TASKS_UPDATE_ENDPOINT = "/api/v1/workplace-tracker-service/tasks/update";
    public static final String TASKS_DELETE_ENDPOINT = "/api/v1/workplace-tracker-service/tasks/delete";
    public static final String TASKS_STATUS_UPDATE_ENDPOINT = "/api/v1/workplace-tracker-service/tasks/status/update";
    public static final String TASKS_PRIORITY_UPDATE_ENDPOINT = "/api/v1/workplace-tracker-service/tasks/priority/update";
    public static final String TASKS_STATS_ENDPOINT = "/api/v1/workplace-tracker-service/tasks/stats";
    public static final String TASKS_SEARCH_ENDPOINT = "/api/v1/workplace-tracker-service/tasks/search";
    public static final String TASKS_OVERDUE_ENDPOINT = "/api/v1/workplace-tracker-service/tasks/overdue";
    public static final String TASKS_BY_STATUS_ENDPOINT = "/api/v1/workplace-tracker-service/tasks/by-status";
    public static final String TASKS_BY_PRIORITY_ENDPOINT = "/api/v1/workplace-tracker-service/tasks/by-priority";
    public static final String TASKS_BULK_UPDATE_ENDPOINT = "/api/v1/workplace-tracker-service/tasks/bulk-update";
    public static final String TASKS_BULK_DELETE_ENDPOINT = "/api/v1/workplace-tracker-service/tasks/bulk-delete";
    public static final String TASKS_DUPLICATE_ENDPOINT = "/api/v1/workplace-tracker-service/tasks/duplicate";

    // Platform User Auth endpoints
    public static final String PLATFORM_AUTH_SIGNUP_ENDPOINT = "/api/v1/workplace-tracker-service/platform-auth/signup";
    public static final String PLATFORM_AUTH_LOGIN_ENDPOINT = "/api/v1/workplace-tracker-service/platform-auth/login";
    public static final String PLATFORM_AUTH_REFRESH_ENDPOINT = "/api/v1/workplace-tracker-service/platform-auth/refresh-token";
    public static final String PLATFORM_AUTH_PROFILE_ENDPOINT = "/api/v1/workplace-tracker-service/platform-auth/profile";

    // Platform User Management endpoints - Super Admin management
    public static final String SUPER_ADMIN_MANAGEMENT_ENDPOINT = "/api/v1/workplace-tracker-service/platform/super-admins";
    public static final String SUPER_ADMIN_BY_TENANT_ENDPOINT = "/api/v1/workplace-tracker-service/platform/super-admins/by-tenant";
    public static final String SUPER_ADMIN_STATUS_ENDPOINT = "/api/v1/workplace-tracker-service/platform/super-admins/status";
    public static final String SUPER_ADMIN_SEARCH_ENDPOINT = "/api/v1/workplace-tracker-service/platform/super-admins/search";
    public static final String SUPER_ADMIN_DETAILS_ENDPOINT = "/api/v1/workplace-tracker-service/platform/super-admins/details";

    // Super Admin Management endpoints - Admin management
    public static final String ADMIN_MANAGEMENT_ENDPOINT = "/api/v1/workplace-tracker-service/super-admin/admins";
    public static final String ADMIN_BY_TENANT_ENDPOINT = "/api/v1/workplace-tracker-service/super-admin/admins/by-tenant";
    public static final String ADMIN_STATUS_ENDPOINT = "/api/v1/workplace-tracker-service/super-admin/admins/status";
    public static final String ADMIN_SEARCH_ENDPOINT = "/api/v1/workplace-tracker-service/super-admin/admins/search";
    public static final String ADMIN_DETAILS_ENDPOINT = "/api/v1/workplace-tracker-service/super-admin/admins/details";

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

    // Tenant Management endpoints (Platform User only) - Updated to use RequestParam instead of PathVariable
    public static final String TENANTS_ENDPOINT = "/api/v1/workplace-tracker-service/tenants";
    public static final String TENANT_ENDPOINT = "/api/v1/workplace-tracker-service/tenant";
    public static final String TENANT_STATUS_ENDPOINT = "/api/v1/workplace-tracker-service/tenant/status";
    public static final String TENANT_SEARCH_ENDPOINT = "/api/v1/workplace-tracker-service/tenant/search";
    public static final String TENANT_STATS_ENDPOINT = "/api/v1/workplace-tracker-service/tenant/stats";
    public static final String ACTIVE_TENANTS_ENDPOINT = "/api/v1/workplace-tracker-service/tenants/active";
    public static final String TENANT_USERS_ENDPOINT = "/api/v1/workplace-tracker-service/tenant/users";
    public static final String TENANT_BY_ID_ENDPOINT = "/api/v1/workplace-tracker-service/tenant/by-id";
    public static final String TENANT_BY_CODE_ENDPOINT = "/api/v1/workplace-tracker-service/tenant/by-code";
    public static final String TENANT_DELETE_ENDPOINT = "/api/v1/workplace-tracker-service/tenant/delete";
    public static final String TENANT_UPDATE_ENDPOINT = "/api/v1/workplace-tracker-service/tenant/update";

    // Subscription Management endpoints (Platform User only)
    public static final String SUBSCRIPTIONS_ENDPOINT = "/api/v1/workplace-tracker-service/subscriptions";
    public static final String ACTIVE_SUBSCRIPTIONS_ENDPOINT = "/api/v1/workplace-tracker-service/subscriptions/active";
    public static final String SUBSCRIPTION_BY_CODE_ENDPOINT = "/api/v1/workplace-tracker-service/subscription/by-code";

    // Subscription Management messages
    public static final String SUCCESS_SUBSCRIPTIONS_RETRIEVED = "Subscriptions retrieved successfully";
    public static final String SUCCESS_ACTIVE_SUBSCRIPTIONS_RETRIEVED = "Active subscriptions retrieved successfully";
    public static final String SUCCESS_SUBSCRIPTION_RETRIEVED = "Subscription retrieved successfully";
    public static final String ERROR_SUBSCRIPTION_NOT_FOUND = "Subscription not found";
    public static final String ERROR_NO_SUBSCRIPTIONS_FOUND = "No subscriptions found";
    public static final String ERROR_INVALID_SUBSCRIPTION_CODE = "Invalid subscription code";

    // Platform Stats endpoints
    public static final String PLATFORM_STATS_ENDPOINT = "/api/v1/workplace-tracker-service/platform/stats";

    // Role codes for platform statistics
    public static final String ROLE_CODE_SUPER_ADMIN = "SUPER_ADMIN";
    public static final String ROLE_CODE_ADMIN = "ADMIN";
    public static final String ROLE_CODE_USER = "USER";
    public static final String ROLE_CODE_MANAGER = "MANAGER";
}
