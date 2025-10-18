package com.sid.app.constants;

public class EndpointConstants {

    private EndpointConstants() {
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
    public static final String TENANT_SUBSCRIPTION_UPDATE_ENDPOINT = "/api/v1/workplace-tracker-service/tenant/subscription/update";

    // Subscription Management endpoints (Platform User only)
    public static final String SUBSCRIPTIONS_ENDPOINT = "/api/v1/workplace-tracker-service/subscriptions";
    public static final String ACTIVE_SUBSCRIPTIONS_ENDPOINT = "/api/v1/workplace-tracker-service/subscriptions/active";
    public static final String SUBSCRIPTION_BY_CODE_ENDPOINT = "/api/v1/workplace-tracker-service/subscription/by-code";

    // Platform Stats endpoints
    public static final String PLATFORM_STATS_ENDPOINT = "/api/v1/workplace-tracker-service/platform/stats";
}
