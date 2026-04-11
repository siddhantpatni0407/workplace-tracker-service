@Auth
Feature: Authentication API - Auth Flow
  As an API consumer
  I want to verify all Authentication API endpoints
  So that registration, login, token refresh, and password management work correctly

  # =============================================================
  # SECTION 1 – REGISTRATION VALIDATION  (no DB pre-requisites)
  # =============================================================

  Scenario: Register fails when name is missing
    When I submit a register request with missing "name" field
    Then the response HTTP status should be 400
    And the response error message should contain "Name is required"

  Scenario: Register fails when email is missing
    When I submit a register request with missing "email" field
    Then the response HTTP status should be 400
    And the response error message should contain "Email is required"

  Scenario: Register fails when password is missing
    When I submit a register request with missing "password" field
    Then the response HTTP status should be 400
    And the response error message should contain "Password is required"

  Scenario: Register fails when role is missing
    When I submit a register request with missing "role" field
    Then the response HTTP status should be 400
    And the response error message should contain "Role is required"

  Scenario: Register fails with an invalid email format
    When I submit a register request with email "not-an-email" role "USER" and tenantUserCode "TU-DUMMY"
    Then the response HTTP status should be 400
    And the response error message should contain "Invalid email format"

  Scenario: Register fails when password is too short
    When I submit a register request with password "short" role "USER" and tenantUserCode "TU-DUMMY"
    Then the response HTTP status should be 400
    And the response error message should contain "Password must be at least 8 characters long"

  Scenario: Register fails with an unsupported role value
    When I submit a register request with role "UNKNOWN_ROLE"
    Then the response HTTP status should be 400
    And the response error message should contain "Invalid role"

  Scenario: Register SUPER_ADMIN fails without platformUserCode
    When I submit a SUPER_ADMIN register request without platformUserCode
    Then the response HTTP status should be 400
    And the response error message should contain "Platform user code is required for SUPER_ADMIN role"

  Scenario: Register SUPER_ADMIN fails without tenantCode
    When I submit a SUPER_ADMIN register request without tenantCode
    Then the response HTTP status should be 400
    And the response error message should contain "Tenant code is required for SUPER_ADMIN role"

  Scenario: Register ADMIN fails without tenantUserCode
    When I submit an ADMIN register request without tenantUserCode
    Then the response HTTP status should be 400
    And the response error message should contain "Tenant user code is required for ADMIN role"

  # ============================================================
  # SECTION 2 – REGISTRATION HAPPY PATH  (requires seed data)
  # ============================================================

  @RequiresSeedData
  Scenario: Register SUPER_ADMIN successfully with valid prerequisites
    Given the prerequisite platform user "PU-CUC-001" and tenant "TNT-CUC-001" exist in the database
    When I register a SUPER_ADMIN with platformUserCode "PU-CUC-001" and tenantCode "TNT-CUC-001"
    Then the response HTTP status should be 200
    And the response status field should be "SUCCESS"
    And the response should contain a valid JWT token

  @RequiresSeedData
  Scenario: Register fails when email already exists
    Given the prerequisite platform user "PU-CUC-001" and tenant "TNT-CUC-001" exist in the database
    And a SUPER_ADMIN is already registered with platformUserCode "PU-CUC-001" and tenantCode "TNT-CUC-001"
    When I register another SUPER_ADMIN with the same email
    Then the response HTTP status should be 400
    And the response error message should contain "Email already exists"

  # =============================================================
  # SECTION 3 – LOGIN SCENARIOS
  # =============================================================

  Scenario: Login fails for a non-existent user
    When I login with email "nobody@cucumber.test" and password "SomePass123!"
    Then the response HTTP status should be 401
    And the response error message should contain "User not found"

  @RequiresSeedData
  Scenario: Login succeeds with valid credentials
    Given a seeded tenant user with email "cuc.superadmin@example.com" and password "TestPass123!" exists
    When I login with email "cuc.superadmin@example.com" and password "TestPass123!"
    Then the response HTTP status should be 200
    And the response status field should be "SUCCESS"
    And the response should contain a valid JWT token

  @RequiresSeedData
  Scenario: Login fails with wrong password
    Given a seeded tenant user with email "cuc.superadmin@example.com" and password "TestPass123!" exists
    When I login with email "cuc.superadmin@example.com" and password "WrongPass999!"
    Then the response HTTP status should be 401
    And the response error message should contain "Invalid credentials"

  # =============================================================
  # SECTION 4 – REFRESH TOKEN SCENARIOS
  # =============================================================

  Scenario: Refresh token fails when no token is provided
    When I request a token refresh without any token
    Then the response HTTP status should be 401
    And the response error message should contain "Missing refresh token"

  Scenario: Refresh token fails with a malformed token
    When I request a token refresh with bearer token "this.is.not.a.valid.jwt"
    Then the response HTTP status should be 401

  # =============================================================
  # SECTION 5 – FORGOT PASSWORD RESET SCENARIOS
  # =============================================================

  Scenario: Reset password fails with an invalid OTP
    When I request a password reset for email "test@cucumber.test" with OTP "000000" and newPassword "NewPass789!"
    Then the response HTTP status should be 400
    And the response error message should contain "Invalid OTP"

  # =============================================================
  # SECTION 6 – CHANGE PASSWORD SCENARIOS
  # =============================================================

  Scenario: Change password fails when no JWT token is provided
    When I send a change password request without a JWT token
    Then the response HTTP status should be 403

  @RequiresSeedData
  Scenario: Change password fails with incorrect current password
    Given a registered and logged-in regular user
    When I change the password with currentPassword "WrongCurrent!" and newPassword "NewPass456!"
    Then the response HTTP status should be 400
    And the response error message should contain "Current password is incorrect"

  @RequiresSeedData
  Scenario: Change password fails when new password is same as current
    Given a registered and logged-in regular user
    When I change the password with currentPassword "TestPass123!" and newPassword "TestPass123!"
    Then the response HTTP status should be 400
    And the response error message should contain "New password must be different from current password"

  @RequiresSeedData
  Scenario: Change password succeeds with valid current and new password
    Given a registered and logged-in regular user
    When I change the password with currentPassword "TestPass123!" and newPassword "UpdatedPass456!"
    Then the response HTTP status should be 200
    And the response status field should be "SUCCESS"
    And the response message should be "Password changed successfully."

