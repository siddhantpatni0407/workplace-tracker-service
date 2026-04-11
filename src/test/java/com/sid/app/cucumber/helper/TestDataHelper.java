package com.sid.app.cucumber.helper;

import com.sid.app.utils.AESUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Helper that creates and tears down test-specific database records for
 * Cucumber happy-path scenarios that need pre-existing entities.
 *
 * <p>All inserted rows use identifiable codes / emails (prefix {@code cuc.}) so
 * that cleanup SQL can safely remove only test data without touching other rows.
 *
 * <p>Seed order matters because of foreign-key constraints:
 * <ol>
 *   <li>platform_user   (references user_role)</li>
 *   <li>tenant          (references app_subscription)</li>
 *   <li>tenant_user     (references tenant, platform_user, user_role)</li>
 *   <li>users           (references tenant_user, user_role)</li>
 * </ol>
 */
@Component
public class TestDataHelper {

    private static final Logger log = LoggerFactory.getLogger(TestDataHelper.class);

    // ----------------------------------------------------------------
    // Public constants – used in step definitions to stay consistent
    // ----------------------------------------------------------------
    public static final String PLATFORM_USER_CODE   = "PU-CUC-001";
    public static final String TENANT_CODE          = "TNT-CUC-001";
    public static final String ADMIN_TU_CODE        = "TU-CUC-ADMIN-01";
    public static final String ADMIN_EMAIL          = "cuc.admin@example.com";
    public static final String SUPER_ADMIN_EMAIL    = "cuc.superadmin@example.com";
    public static final String REGULAR_USER_EMAIL   = "cuc.user@example.com";
    public static final String REGULAR_USER_MOBILE  = "+9990010001";
    public static final String TEST_PASSWORD        = "TestPass123!";

    private final JdbcTemplate jdbc;
    private final AESUtils aesUtils;

    public TestDataHelper(JdbcTemplate jdbc, AESUtils aesUtils) {
        this.jdbc = jdbc;
        this.aesUtils = aesUtils;
    }

    // ---------------------------------------------------------------
    // Seed helpers
    // ---------------------------------------------------------------

    /**
     * Inserts a platform_user row (role = PLATFORM_USER) that can be referenced
     * during SUPER_ADMIN registration tests.
     */
    public void seedPlatformUser() {
        String encPwd = aesUtils.encrypt("PlatformSeed123!");
        Long platformRoleId = jdbc.queryForObject(
                "SELECT role_id FROM user_role WHERE role = 'PLATFORM_USER'", Long.class);

        jdbc.update(
                "INSERT INTO platform_user " +
                "(role_id, platform_user_code, name, email, mobile_number, " +
                " password, password_encryption_key_version, is_active, login_attempts, account_locked) " +
                "VALUES (?,?,?,?,?,?,1,true,0,false) " +
                "ON CONFLICT (platform_user_code) DO NOTHING",
                platformRoleId, PLATFORM_USER_CODE,
                "Cuc Platform User", "cuc.platform@test.com", "+9999900001", encPwd);

        log.info("TestData: platform_user seeded (code={})", PLATFORM_USER_CODE);
    }

    /**
     * Inserts a tenant row that can be referenced during SUPER_ADMIN registration tests.
     */
    public void seedTenant() {
        jdbc.update(
                "INSERT INTO tenant " +
                "(tenant_name, tenant_code, app_subscription_id, contact_email, is_active) " +
                "VALUES (?,?,1,?,true) " +
                "ON CONFLICT (tenant_code) DO NOTHING",
                "Cuc Test Corp", TENANT_CODE, "cuc-corp@test.com");

        log.info("TestData: tenant seeded (code={})", TENANT_CODE);
    }

    /**
     * Inserts an ADMIN tenant_user row directly (bypasses the registration API so that
     * USER registration tests can reference a valid {@code tenantUserCode}).
     *
     * @return the generated {@code tenant_user_id}
     */
    public Long seedAdminTenantUser() {
        seedPlatformUser();
        seedTenant();

        String encPwd = aesUtils.encrypt(TEST_PASSWORD);
        Long adminRoleId = jdbc.queryForObject(
                "SELECT role_id FROM user_role WHERE role = 'ADMIN'", Long.class);
        Long platformUserId = jdbc.queryForObject(
                "SELECT platform_user_id FROM platform_user WHERE platform_user_code = ?",
                Long.class, PLATFORM_USER_CODE);
        Long tenantId = jdbc.queryForObject(
                "SELECT tenant_id FROM tenant WHERE tenant_code = ?",
                Long.class, TENANT_CODE);

        jdbc.update(
                "INSERT INTO tenant_user " +
                "(tenant_id, platform_user_id, role_id, tenant_user_code, name, email, " +
                " mobile_number, password, password_encryption_key_version, " +
                " is_active, login_attempts, account_locked) " +
                "VALUES (?,?,?,?,?,?,?,?,1,true,0,false) " +
                "ON CONFLICT (tenant_user_code) DO NOTHING",
                tenantId, platformUserId, adminRoleId, ADMIN_TU_CODE,
                "Cuc Admin User", ADMIN_EMAIL, "+9999900002", encPwd);

        log.info("TestData: admin tenant_user seeded (email={})", ADMIN_EMAIL);

        return jdbc.queryForObject(
                "SELECT tenant_user_id FROM tenant_user WHERE tenant_user_code = ?",
                Long.class, ADMIN_TU_CODE);
    }

    /**
     * Inserts a SUPER_ADMIN tenant_user row directly (used by Login / Change-Password tests
     * so they do not depend on the Register API being tested separately).
     */
    public void seedSuperAdminTenantUser() {
        seedPlatformUser();
        seedTenant();

        String encPwd = aesUtils.encrypt(TEST_PASSWORD);
        Long saRoleId = jdbc.queryForObject(
                "SELECT role_id FROM user_role WHERE role = 'SUPER_ADMIN'", Long.class);
        Long platformUserId = jdbc.queryForObject(
                "SELECT platform_user_id FROM platform_user WHERE platform_user_code = ?",
                Long.class, PLATFORM_USER_CODE);
        Long tenantId = jdbc.queryForObject(
                "SELECT tenant_id FROM tenant WHERE tenant_code = ?",
                Long.class, TENANT_CODE);

        jdbc.update(
                "INSERT INTO tenant_user " +
                "(tenant_id, platform_user_id, role_id, tenant_user_code, name, email, " +
                " mobile_number, password, password_encryption_key_version, " +
                " is_active, login_attempts, account_locked) " +
                "VALUES (?,?,?,?,?,?,?,?,1,true,0,false) " +
                "ON CONFLICT (tenant_user_code) DO NOTHING",
                tenantId, platformUserId, saRoleId, "TU-CUC-SA-001",
                "Cuc Super Admin", SUPER_ADMIN_EMAIL, "+9999900003", encPwd);

        log.info("TestData: super_admin tenant_user seeded (email={})", SUPER_ADMIN_EMAIL);
    }

    /**
     * Registers a regular USER (in the {@code users} table) via direct JDBC so that
     * change-password scenarios have a valid subject to work with.
     */
    public void seedRegularUser() {
        Long adminTenantUserId = seedAdminTenantUser();

        String encPwd = aesUtils.encrypt(TEST_PASSWORD);
        Long userRoleId = jdbc.queryForObject(
                "SELECT role_id FROM user_role WHERE role = 'USER'", Long.class);

        jdbc.update(
                "INSERT INTO users " +
                "(tenant_user_id, name, email, mobile_number, password, " +
                " password_encryption_key_version, role_id, is_active, login_attempts, account_locked) " +
                "VALUES (?,?,?,?,?,1,?,true,0,false) " +
                "ON CONFLICT (email) DO NOTHING",
                adminTenantUserId, "Cuc Regular User",
                REGULAR_USER_EMAIL, REGULAR_USER_MOBILE, encPwd, userRoleId);

        log.info("TestData: regular user seeded (email={})", REGULAR_USER_EMAIL);
    }

    // ---------------------------------------------------------------
    // Cleanup
    // ---------------------------------------------------------------

    /**
     * Removes all rows that were inserted by this helper.
     * Called from {@link com.sid.app.cucumber.hooks.CucumberHooks} after each scenario.
     */
    public void cleanupSeedData() {
        jdbc.update("DELETE FROM users        WHERE email LIKE 'cuc.%'");
        jdbc.update("DELETE FROM tenant_user  WHERE email LIKE 'cuc.%'");
        jdbc.update("DELETE FROM platform_user WHERE platform_user_code = ?", PLATFORM_USER_CODE);
        jdbc.update("DELETE FROM tenant        WHERE tenant_code = ?",        TENANT_CODE);
        log.debug("TestData: seed cleanup complete");
    }
}
