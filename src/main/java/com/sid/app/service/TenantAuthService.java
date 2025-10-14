package com.sid.app.service;

import com.sid.app.model.AuthResponse;
import com.sid.app.model.LoginRequest;
import com.sid.app.model.RegisterRequest;

/**
 * Service interface for handling tenant-based user authentication
 * Supports SUPER_ADMIN, ADMIN, and USER roles with tenant validation
 */
public interface TenantAuthService {

    /**
     * Register a new tenant user (SUPER_ADMIN, ADMIN, or USER)
     * SUPER_ADMIN and ADMIN are saved to tenant_user table
     * USER is saved to users table with reference to tenant_user
     */
    AuthResponse registerTenantUser(RegisterRequest request);

    /**
     * Login for tenant users (SUPER_ADMIN, ADMIN, USER)
     * Updates last_login_time on successful login
     */
    AuthResponse loginTenantUser(LoginRequest request);
}
