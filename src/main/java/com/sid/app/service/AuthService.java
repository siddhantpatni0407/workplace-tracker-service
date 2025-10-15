package com.sid.app.service;

import com.sid.app.auth.JwtUtil;
import com.sid.app.constants.AppConstants;
import com.sid.app.entity.User;
import com.sid.app.entity.UserRole;
import com.sid.app.entity.Tenant;
import com.sid.app.entity.TenantUser;
import com.sid.app.exception.UserNotFoundException;
import com.sid.app.model.AuthResponse;
import com.sid.app.model.LoginRequest;
import com.sid.app.model.RegisterRequest;
import com.sid.app.model.ForgotPasswordResetRequest;
import com.sid.app.model.ResponseDTO;
import com.sid.app.repository.UserRepository;
import com.sid.app.repository.UserRoleRepository;
import com.sid.app.repository.TenantRepository;
import com.sid.app.repository.TenantUserRepository;
import com.sid.app.repository.PlatformUserRepository;
import com.sid.app.utils.AESUtils;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AuthService - handles registration, login, refresh token, password reset, etc.
 * Enhanced with tenant-based user support for SUPER_ADMIN, ADMIN, and USER roles.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final TenantRepository tenantRepository;
    private final TenantUserRepository tenantUserRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AESUtils aesUtils;
    private final EncryptionKeyService encryptionKeyService;
    private final CodeGenerationService codeGenerationService;
    private final PlatformUserRepository platformUserRepository;

    private static final ConcurrentHashMap<String, String> otpStore = new ConcurrentHashMap<>();

    /**
     * Register new user with tenant support.
     * - SUPER_ADMIN, ADMIN: saves to tenant_user table + requires tenantCode
     * - USER: saves to users table + tenant_user table + requires tenantCode
     * - Other roles: saves to users table without tenant validation (existing behavior)
     */
    public AuthResponse register(RegisterRequest request) {
        // Validate tenant code for tenant-based roles
        if (isTenantBasedRole(request.getRole())) {
            if (request.getTenantCode() == null || request.getTenantCode().trim().isEmpty()) {
                return new AuthResponse(null, null, null, null,
                        AppConstants.STATUS_FAILED,
                        "Tenant code is required for " + request.getRole() + " role registration",
                        null, null, null, null);
            }

            // Validate tenant exists and is active
            Optional<Tenant> tenantOpt = tenantRepository.findActiveByTenantCode(request.getTenantCode());
            return tenantOpt.map(tenant -> registerTenantBasedUser(request, tenant))
                    .orElseGet(() -> new AuthResponse(null, null, null, null,
                            AppConstants.STATUS_FAILED,
                            "Invalid or inactive tenant code: " + request.getTenantCode(),
                            null, null, null, null));

        }

        // Existing user registration logic for non-tenant roles
        return registerRegularUser(request);
    }

    /**
     * Register tenant-based users (SUPER_ADMIN, ADMIN only)
     */
    private AuthResponse registerTenantBasedUser(RegisterRequest request, Tenant tenant) {
        try {
            // Get role information
            Optional<UserRole> roleOpt = userRoleRepository.findByRoleIgnoreCase(request.getRole());
            if (roleOpt.isEmpty()) {
                return new AuthResponse(null, null, null, null,
                        AppConstants.STATUS_FAILED,
                        "Role not found: " + request.getRole(),
                        null, null, null, null);
            }

            UserRole role = roleOpt.get();

            // Check for existing users in both tables
            if (tenantUserRepository.existsByEmail(request.getEmail())) {
                return new AuthResponse(null, null, null, null,
                        AppConstants.STATUS_FAILED,
                        AppConstants.ERROR_MESSAGE_EMAIL_EXISTS,
                        null, null, null, null);
            }

            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                return new AuthResponse(null, null, null, null,
                        AppConstants.STATUS_FAILED,
                        AppConstants.ERROR_MESSAGE_EMAIL_EXISTS,
                        null, null, null, null);
            }

            if (request.getMobileNumber() != null && tenantUserRepository.existsByMobileNumber(request.getMobileNumber())) {
                return new AuthResponse(null, null, null, null,
                        AppConstants.STATUS_FAILED,
                        AppConstants.ERROR_MESSAGE_MOBILE_EXISTS,
                        null, null, null, null);
            }

            // Encrypt password
            String encryptedPassword = aesUtils.encrypt(request.getPassword());

            // Only SUPER_ADMIN and ADMIN are handled here
            if ("SUPER_ADMIN".equalsIgnoreCase(request.getRole()) || "ADMIN".equalsIgnoreCase(request.getRole())) {
                return registerToTenantUserTable(request, tenant, role, encryptedPassword);
            }

            return new AuthResponse(null, null, null, null,
                    AppConstants.STATUS_FAILED,
                    "Unsupported tenant role: " + request.getRole(),
                    null, null, null, null);

        } catch (Exception e) {
            log.error("Error during tenant-based user registration: {}", e.getMessage(), e);
            return new AuthResponse(null, null, null, null,
                    AppConstants.STATUS_FAILED,
                    AppConstants.ERROR_MESSAGE_REGISTRATION,
                    null, null, null, null);
        }
    }

    /**
     * Register SUPER_ADMIN and ADMIN to tenant_user table
     */
    private AuthResponse registerToTenantUserTable(RegisterRequest request, Tenant tenant, UserRole role, String encryptedPassword) {
        // Check if SUPER_ADMIN already exists for this tenant
        if ("SUPER_ADMIN".equalsIgnoreCase(request.getRole())) {
            if (!tenantUserRepository.findByTenantIdAndRoleId(tenant.getTenantId(), role.getRoleId()).isEmpty()) {
                return new AuthResponse(null, null, null, null,
                        AppConstants.STATUS_FAILED,
                        "A SUPER_ADMIN already exists for this tenant",
                        null, null, null, null);
            }
        }

        TenantUser tenantUser = new TenantUser();
        tenantUser.setTenantId(tenant.getTenantId());
        tenantUser.setPlatformUserId(1L); // Default platform user ID
        tenantUser.setRoleId(role.getRoleId());
        tenantUser.setName(request.getName());
        tenantUser.setEmail(request.getEmail());
        tenantUser.setMobileNumber(request.getMobileNumber());
        tenantUser.setPassword(encryptedPassword);
        tenantUser.setPasswordEncryptionKeyVersion(encryptionKeyService.getLatestKey().getKeyVersion());
        tenantUser.setIsActive(true);
        tenantUser.setLoginAttempts(0);
        tenantUser.setAccountLocked(false);

        tenantUser = tenantUserRepository.save(tenantUser);

        String jwtToken = jwtUtil.generateTokenWithUserDetails(
                tenantUser.getEmail(),
                tenantUser.getTenantUserId(),
                tenantUser.getName(),
                role.getRole()
        );

        log.info("Tenant user registration successful for email: {} with role: {}", request.getEmail(), request.getRole());

        return new AuthResponse(
                jwtToken,
                role.getRole(),
                tenantUser.getTenantUserId(),
                tenantUser.getName(),
                AppConstants.STATUS_SUCCESS,
                AppConstants.SUCCESS_MESSAGE_REGISTRATION_SUCCESSFUL,
                LocalDateTime.now(),
                true,
                0,
                false
        );
    }

    /**
     * Register regular users (non-tenant based) - existing logic
     */
    private AuthResponse registerRegularUser(RegisterRequest request) {
        Optional<User> existingUser = userRepository.findByEmailOrMobileNumber(
                request.getEmail(), request.getMobileNumber()
        );

        if (existingUser.isPresent()) {
            User foundUser = existingUser.get();
            if (foundUser.getEmail().equals(request.getEmail())) {
                return new AuthResponse(null, null, null, null,
                        AppConstants.STATUS_FAILED,
                        AppConstants.ERROR_MESSAGE_EMAIL_EXISTS,
                        null, null, null, null);
            } else {
                return new AuthResponse(null, null, null, null,
                        AppConstants.STATUS_FAILED,
                        AppConstants.ERROR_MESSAGE_MOBILE_EXISTS,
                        null, null, null, null);
            }
        }

        try {
            // encrypt password using AES utils (your existing approach)
            String encryptedPassword = aesUtils.encrypt(request.getPassword());

            User newUser = new User();
            newUser.setName(request.getName());
            newUser.setEmail(request.getEmail());
            newUser.setMobileNumber(request.getMobileNumber());
            newUser.setPassword(encryptedPassword);

            // map role string to role_id (throws IllegalArgumentException if invalid)
            Long roleId = resolveRoleId(request.getRole());
            newUser.setRoleId(roleId);

            newUser.setPasswordEncryptionKeyVersion(encryptionKeyService.getLatestKey().getKeyVersion());
            newUser.setIsActive(Boolean.FALSE);
            newUser.setLoginAttempts(0);
            newUser.setAccountLocked(Boolean.FALSE);

            User savedUser = userRepository.save(newUser);

            // resolve role name for response
            String roleName = resolveRoleName(savedUser.getRoleId());

            // Generate JWT token with user details
            String jwtToken = jwtUtil.generateTokenWithUserDetails(
                    savedUser.getEmail(),
                    savedUser.getUserId(),
                    savedUser.getName(),
                    roleName
            );

            return new AuthResponse(
                    jwtToken,
                    roleName,
                    savedUser.getUserId(),
                    savedUser.getName(),
                    AppConstants.STATUS_SUCCESS,
                    AppConstants.SUCCESS_MESSAGE_REGISTRATION_SUCCESSFUL,
                    null,
                    true,
                    0,
                    false
            );
        } catch (IllegalArgumentException iae) {
            log.warn("register() : Invalid role specified: {}", request.getRole());
            return new AuthResponse(null, null, null, null,
                    AppConstants.STATUS_FAILED,
                    iae.getMessage(),
                    null, null, null, null);
        } catch (Exception e) {
            log.error("Error encrypting password or saving user: {}", e.getMessage(), e);
            return new AuthResponse(null, null, null, null,
                    AppConstants.STATUS_FAILED,
                    AppConstants.ERROR_MESSAGE_REGISTRATION,
                    null, null, null, null);
        }
    }

    /**
     * Login using email + password (AES-encrypted password in DB).
     * Handles both tenant_user table (SUPER_ADMIN, ADMIN) and users table (USER) logins.
     */
    public AuthResponse login(LoginRequest request) {
        // First check in tenant_user table for SUPER_ADMIN/ADMIN
        Optional<TenantUser> tenantUserOpt = tenantUserRepository.findActiveByEmail(request.getEmail());
        if (tenantUserOpt.isPresent()) {
            return loginTenantUser(request, tenantUserOpt.get());
        }

        // Then check in users table for USER role
        Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());
        if (optionalUser.isEmpty()) {
            return new AuthResponse(null, null, null, null,
                    AppConstants.STATUS_FAILED,
                    AppConstants.ERROR_MESSAGE_USER_NOT_FOUND,
                    null, null, null, null);
        }

        return loginRegularUser(request, optionalUser.get());
    }

    /**
     * Login for tenant users (SUPER_ADMIN, ADMIN)
     */
    private AuthResponse loginTenantUser(LoginRequest request, TenantUser tenantUser) {
        if (!tenantUser.getIsActive()) {
            return new AuthResponse(null, null, null, null,
                    AppConstants.STATUS_FAILED,
                    AppConstants.ERROR_MESSAGE_INACTIVE_ACCOUNT,
                    null, false, tenantUser.getLoginAttempts(), tenantUser.getAccountLocked());
        }

        if (Boolean.TRUE.equals(tenantUser.getAccountLocked())) {
            return new AuthResponse(null, null, null, null,
                    AppConstants.STATUS_FAILED,
                    AppConstants.ERROR_MESSAGE_ACCOUNT_LOCKED,
                    null, true, tenantUser.getLoginAttempts(), true);
        }

        try {
            String decryptedPassword = aesUtils.decrypt(tenantUser.getPassword(),
                    tenantUser.getPasswordEncryptionKeyVersion());

            if (!request.getPassword().equals(decryptedPassword)) {
                tenantUser.setLoginAttempts(tenantUser.getLoginAttempts() + 1);
                if (tenantUser.getLoginAttempts() >= 5) {
                    tenantUser.setAccountLocked(true);
                }
                tenantUserRepository.save(tenantUser);
                return new AuthResponse(null, null, null, null,
                        AppConstants.STATUS_FAILED,
                        AppConstants.ERROR_MESSAGE_INVALID_LOGIN,
                        null, tenantUser.getIsActive(), tenantUser.getLoginAttempts(), tenantUser.getAccountLocked());
            }
        } catch (Exception e) {
            log.error("Error decrypting password for tenant user {}: {}", request.getEmail(), e.getMessage());
            return new AuthResponse(null, null, null, null,
                    AppConstants.STATUS_FAILED,
                    AppConstants.ERROR_MESSAGE_LOGIN,
                    null, tenantUser.getIsActive(), tenantUser.getLoginAttempts(), tenantUser.getAccountLocked());
        }

        // Update last login time and reset login attempts
        tenantUser.setLoginAttempts(0);
        tenantUser.setLastLoginTime(LocalDateTime.now());
        tenantUserRepository.save(tenantUser);

        Optional<UserRole> roleOpt = userRoleRepository.findById(tenantUser.getRoleId());
        String roleName = roleOpt.map(UserRole::getRole).orElse("UNKNOWN");

        String jwtToken = jwtUtil.generateTokenWithUserDetails(
                tenantUser.getEmail(),
                tenantUser.getTenantUserId(),
                tenantUser.getName(),
                roleName
        );

        return new AuthResponse(
                jwtToken,
                roleName,
                tenantUser.getTenantUserId(),
                tenantUser.getName(),
                AppConstants.STATUS_SUCCESS,
                AppConstants.LOGIN_SUCCESSFUL_MESSAGE,
                tenantUser.getLastLoginTime(),
                true,
                0,
                false
        );
    }

    /**
     * Login for regular users (USER role in users table)
     */
    private AuthResponse loginRegularUser(LoginRequest request, User user) {
        if (!user.getIsActive()) {
            return new AuthResponse(null, null, null, null,
                    AppConstants.STATUS_FAILED,
                    AppConstants.ERROR_MESSAGE_INACTIVE_ACCOUNT,
                    null, false, user.getLoginAttempts(), user.getAccountLocked());
        }

        if (Boolean.TRUE.equals(user.getAccountLocked())) {
            return new AuthResponse(null, null, null, null,
                    AppConstants.STATUS_FAILED,
                    AppConstants.ERROR_MESSAGE_ACCOUNT_LOCKED,
                    null, true, user.getLoginAttempts(), true);
        }

        try {
            String decryptedPassword = aesUtils.decrypt(user.getPassword(),
                    user.getPasswordEncryptionKeyVersion());

            if (!request.getPassword().equals(decryptedPassword)) {
                user.setLoginAttempts(user.getLoginAttempts() + 1);

                if (user.getLoginAttempts() >= 5) {
                    user.setAccountLocked(true);
                }
                userRepository.save(user);

                return new AuthResponse(null, null, null, null,
                        AppConstants.STATUS_FAILED,
                        AppConstants.ERROR_MESSAGE_INVALID_LOGIN,
                        null, user.getIsActive(), user.getLoginAttempts(), user.getAccountLocked());
            }
        } catch (Exception e) {
            log.error("Error decrypting password for user {}: {}", request.getEmail(), e.getMessage());
            return new AuthResponse(null, null, null, null,
                    AppConstants.STATUS_FAILED,
                    AppConstants.ERROR_MESSAGE_LOGIN,
                    null, user.getIsActive(), user.getLoginAttempts(), user.getAccountLocked());
        }

        // Update last login time and reset login attempts
        user.setLoginAttempts(0);
        user.setLastLoginTime(LocalDateTime.now());
        userRepository.save(user);

        String roleName = resolveRoleName(user.getRoleId());

        // Generate JWT token with user details
        String jwtToken = jwtUtil.generateTokenWithUserDetails(
                user.getEmail(),
                user.getUserId(),
                user.getName(),
                roleName
        );

        return new AuthResponse(
                jwtToken,
                roleName,
                user.getUserId(),
                user.getName(),
                AppConstants.STATUS_SUCCESS,
                AppConstants.LOGIN_SUCCESSFUL_MESSAGE,
                user.getLastLoginTime(),
                true,
                0,
                false
        );
    }

    // ...existing methods (createRefreshCookieForUser, resetPassword, refreshToken, changePassword)...

    /**
     * Create and attach a refresh token cookie for the given user's email.
     */
    public void createRefreshCookieForUser(String email, HttpServletResponse servletResponse) {
        long refreshTtlMs = 7L * 24L * 60L * 60L * 1000L;
        String refreshToken = jwtUtil.generateToken(email, null, refreshTtlMs);

        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(Duration.ofMillis(refreshTtlMs))
                .sameSite("Lax")
                .build();

        servletResponse.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public ResponseEntity<ResponseDTO<Void>> resetPassword(ForgotPasswordResetRequest request) {
        String email = request.getEmail();
        String otp = request.getOtp();
        String newPassword = request.getNewPassword();

        if (!otpStore.containsKey(email) || !otpStore.get(email).equals(otp)) {
            return ResponseEntity.badRequest()
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "Invalid OTP.", null));
        }

        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(404)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, AppConstants.ERROR_MESSAGE_USER_NOT_FOUND, null));
        }

        try {
            User user = userOptional.get();
            user.setPassword(aesUtils.encrypt(newPassword));
            userRepository.save(user);
            otpStore.remove(email);

            return ResponseEntity.ok(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, "Password reset successful.", null));
        } catch (Exception e) {
            log.error("Error resetting password for {}: {}", email, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "Error resetting password.", null));
        }
    }

    public AuthResponse refreshToken(String refreshToken, HttpServletResponse servletResponse) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return new AuthResponse(null, null, null, null,
                    AppConstants.STATUS_FAILED, "Missing refresh token.", null, null, null, null);
        }

        try {
            String username = jwtUtil.extractUsername(refreshToken);
            Optional<User> optUser = userRepository.findByEmail(username);
            if (optUser.isEmpty()) {
                return new AuthResponse(null, null, null, null,
                        AppConstants.STATUS_FAILED, AppConstants.ERROR_MESSAGE_USER_NOT_FOUND, null, null, null, null);
            }
            User user = optUser.get();
            if (!user.getIsActive()) {
                return new AuthResponse(null, null, null, null,
                        AppConstants.STATUS_FAILED, AppConstants.ERROR_MESSAGE_INACTIVE_ACCOUNT, null, null, null, null);
            }

            String newAccessToken = jwtUtil.generateToken(user.getEmail());
            long refreshTtlMs = 7L * 24L * 60L * 60L * 1000L;
            String newRefreshToken = jwtUtil.generateToken(user.getEmail(), null, refreshTtlMs);

            ResponseCookie cookie = ResponseCookie.from("refreshToken", newRefreshToken)
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(Duration.ofMillis(refreshTtlMs))
                    .sameSite("Lax")
                    .build();

            servletResponse.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
            String roleName = resolveRoleName(user.getRoleId());

            return new AuthResponse(
                    newAccessToken,
                    roleName,
                    user.getUserId(),
                    user.getName(),
                    AppConstants.STATUS_SUCCESS,
                    "Token refreshed",
                    user.getLastLoginTime(),
                    true,
                    user.getLoginAttempts(),
                    user.getAccountLocked()
            );
        } catch (Exception ex) {
            log.error("Error during refreshToken: {}", ex.getMessage(), ex);
            return new AuthResponse(null, null, null, null,
                    AppConstants.STATUS_FAILED, "Invalid refresh token.", null, null, null, null);
        }
    }

    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        log.info("changePassword() : Attempting password change for userId={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("changePassword() : User not found userId={}", userId);
                    return new UserNotFoundException("User not found with ID: " + userId);
                });

        try {
            String decrypted = aesUtils.decrypt(user.getPassword(), user.getPasswordEncryptionKeyVersion());

            if (!decrypted.equals(currentPassword)) {
                log.info("changePassword() : Current password mismatch for userId={}", userId);
                throw new IllegalArgumentException("Current password is incorrect.");
            }

            if (currentPassword.equals(newPassword)) {
                log.info("changePassword() : New password same as current for userId={}", userId);
                throw new IllegalArgumentException("New password must be different from current password.");
            }

            if (newPassword.length() < 8) {
                log.info("changePassword() : New password too short for userId={}", userId);
                throw new IllegalArgumentException("New password must be at least 8 characters long.");
            }

            String encryptedNew = aesUtils.encrypt(newPassword);
            int updated = userRepository.updatePassword(
                    userId,
                    encryptedNew,
                    encryptionKeyService.getLatestKey().getKeyVersion()
            );

            if (updated == 0) {
                log.warn("changePassword() : No rows updated for userId={}", userId);
                throw new UserNotFoundException("User not found with ID: " + userId);
            }

            log.info("changePassword() : Password updated successfully for userId={}", userId);
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("changePassword() : Error while changing password for userId={}: {}", userId, ex.getMessage(), ex);
            throw new RuntimeException("Failed to change password. Please try again later.");
        }
    }

    // ---------------------------
    // Helper methods
    // ---------------------------

    /**
     * Check if the role is tenant-based (requires tenant code for registration).
     */
    private boolean isTenantBasedRole(String role) {
        return "SUPER_ADMIN".equalsIgnoreCase(role) ||
                "ADMIN".equalsIgnoreCase(role);
    }

    /**
     * Resolve role name from DB by roleId.
     */
    private String resolveRoleName(Long roleId) {
        if (roleId == null) {
            return "USER";
        }
        return userRoleRepository.findById(roleId)
                .map(UserRole::getRole)
                .orElse("USER");
    }

    /**
     * Resolve roleId by role name.
     */
    private Long resolveRoleId(String roleName) {
        String effective = (roleName == null || roleName.isBlank()) ? "USER" : roleName.trim();
        Optional<UserRole> roleOpt = userRoleRepository.findByRole(effective);
        if (roleOpt.isPresent()) {
            return roleOpt.get().getRoleId();
        } else {
            throw new IllegalArgumentException("Invalid role: " + roleName);
        }
    }
}
