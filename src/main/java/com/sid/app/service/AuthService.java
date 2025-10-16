package com.sid.app.service;

import com.sid.app.auth.JwtUtil;
import com.sid.app.constants.AppConstants;
import com.sid.app.entity.User;
import com.sid.app.entity.UserRole;
import com.sid.app.entity.Tenant;
import com.sid.app.entity.TenantUser;
import com.sid.app.entity.PlatformUser;
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
    private final PlatformUserRepository platformUserRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AESUtils aesUtils;
    private final EncryptionKeyService encryptionKeyService;
    private final CodeGenerationService codeGenerationService;

    private static final ConcurrentHashMap<String, String> otpStore = new ConcurrentHashMap<>();

    /**
     * Enhanced register method with role-based code validation:
     * - SUPER_ADMIN: requires platformUserCode + tenantCode
     * - ADMIN: requires tenantCode
     * - USER/MANAGER: requires tenantUserCode
     */
    public AuthResponse register(RegisterRequest request) {
        try {
            // Validate role exists
            Optional<UserRole> roleOpt = userRoleRepository.findByRoleIgnoreCase(request.getRole());
            if (roleOpt.isEmpty()) {
                return createErrorResponse("Role not found: " + request.getRole());
            }

            UserRole role = roleOpt.get();

            // Validate required codes based on role
            AuthResponse validationResponse = validateRoleBasedCodes(request, role);
            if (!AppConstants.STATUS_SUCCESS.equals(validationResponse.getStatus())) {
                return validationResponse;
            }

            // Check for existing users
            AuthResponse existingUserCheck = checkExistingUsers(request);
            if (!AppConstants.STATUS_SUCCESS.equals(existingUserCheck.getStatus())) {
                return existingUserCheck;
            }

            // Route to appropriate registration method based on role
            switch (request.getRole().toUpperCase()) {
                case "SUPER_ADMIN":
                    return registerSuperAdmin(request, role);
                case "ADMIN":
                    return registerAdmin(request, role);
                case "USER":
                case "MANAGER":
                    return registerUserOrManager(request, role);
                default:
                    return createErrorResponse("Unsupported role: " + request.getRole());
            }

        } catch (Exception e) {
            log.error("Error during user registration: {}", e.getMessage(), e);
            return createErrorResponse(AppConstants.ERROR_MESSAGE_REGISTRATION);
        }
    }

    /**
     * Validate required codes based on role
     */
    private AuthResponse validateRoleBasedCodes(RegisterRequest request, UserRole role) {
        String roleName = request.getRole().toUpperCase();

        switch (roleName) {
            case "SUPER_ADMIN":
                if (isBlank(request.getPlatformUserCode())) {
                    return createErrorResponse("Platform user code is required for SUPER_ADMIN role");
                }
                if (isBlank(request.getTenantCode())) {
                    return createErrorResponse("Tenant code is required for SUPER_ADMIN role");
                }

                // Validate platform user code
                Optional<PlatformUser> platformUserOpt = platformUserRepository.findByPlatformUserCode(request.getPlatformUserCode());
                if (platformUserOpt.isEmpty() || !platformUserOpt.get().getIsActive()) {
                    return createErrorResponse("Invalid or inactive platform user code: " + request.getPlatformUserCode());
                }

                // Validate tenant code
                Optional<Tenant> tenantOpt = tenantRepository.findActiveByTenantCode(request.getTenantCode());
                if (tenantOpt.isEmpty()) {
                    return createErrorResponse("Invalid or inactive tenant code: " + request.getTenantCode());
                }
                break;

            case "ADMIN":
                if (isBlank(request.getTenantCode())) {
                    return createErrorResponse("Tenant code is required for ADMIN role");
                }

                // Validate tenant code
                Optional<Tenant> adminTenantOpt = tenantRepository.findActiveByTenantCode(request.getTenantCode());
                if (adminTenantOpt.isEmpty()) {
                    return createErrorResponse("Invalid or inactive tenant code: " + request.getTenantCode());
                }
                break;

            case "USER":
            case "MANAGER":
                if (isBlank(request.getTenantUserCode())) {
                    return createErrorResponse("Tenant user code is required for " + roleName + " role");
                }

                // Validate tenant user code (admin mapping)
                Optional<TenantUser> adminUserOpt = tenantUserRepository.findActiveByTenantUserCode(request.getTenantUserCode());
                if (adminUserOpt.isEmpty()) {
                    return createErrorResponse("Invalid or inactive tenant user code: " + request.getTenantUserCode());
                }

                // Ensure the tenant user code belongs to an ADMIN
                UserRole adminRole = userRoleRepository.findById(adminUserOpt.get().getRoleId()).orElse(null);
                if (adminRole == null || !"ADMIN".equalsIgnoreCase(adminRole.getRole())) {
                    return createErrorResponse("Tenant user code must belong to an ADMIN user");
                }
                break;

            default:
                return createErrorResponse("Unsupported role: " + roleName);
        }

        return new AuthResponse(null, null, null, null, AppConstants.STATUS_SUCCESS, null, null, null, null, null);
    }

    /**
     * Check for existing users in all relevant tables
     */
    private AuthResponse checkExistingUsers(RegisterRequest request) {
        // Check in users table
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return createErrorResponse(AppConstants.ERROR_MESSAGE_EMAIL_EXISTS);
        }

        // Check in tenant_user table
        if (tenantUserRepository.existsByEmail(request.getEmail())) {
            return createErrorResponse(AppConstants.ERROR_MESSAGE_EMAIL_EXISTS);
        }

        // Check in platform_user table
        if (platformUserRepository.findByEmail(request.getEmail()).isPresent()) {
            return createErrorResponse(AppConstants.ERROR_MESSAGE_EMAIL_EXISTS);
        }

        // Check mobile number if provided
        if (request.getMobileNumber() != null) {
            if (userRepository.findByMobileNumber(request.getMobileNumber()).isPresent() ||
                tenantUserRepository.existsByMobileNumber(request.getMobileNumber()) ||
                platformUserRepository.findByMobileNumber(request.getMobileNumber()).isPresent()) {
                return createErrorResponse(AppConstants.ERROR_MESSAGE_MOBILE_EXISTS);
            }
        }

        return new AuthResponse(null, null, null, null, AppConstants.STATUS_SUCCESS, null, null, null, null, null);
    }

    /**
     * Register SUPER_ADMIN to tenant_user table
     */
    private AuthResponse registerSuperAdmin(RegisterRequest request, UserRole role) {
        try {
            PlatformUser platformUser = platformUserRepository.findByPlatformUserCode(request.getPlatformUserCode()).get();
            Tenant tenant = tenantRepository.findActiveByTenantCode(request.getTenantCode()).get();

            // Check if SUPER_ADMIN already exists for this tenant
            if (!tenantUserRepository.findByTenantIdAndRoleId(tenant.getTenantId(), role.getRoleId()).isEmpty()) {
                return createErrorResponse("A SUPER_ADMIN already exists for this tenant");
            }

            String encryptedPassword = aesUtils.encrypt(request.getPassword());

            TenantUser tenantUser = new TenantUser();
            tenantUser.setTenantId(tenant.getTenantId());
            tenantUser.setPlatformUserId(platformUser.getPlatformUserId());
            tenantUser.setRoleId(role.getRoleId());
            tenantUser.setTenantUserCode(codeGenerationService.generateTenantUserCode());
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

            log.info("SUPER_ADMIN registration successful for email: {}", request.getEmail());

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
        } catch (Exception e) {
            log.error("Error registering SUPER_ADMIN: {}", e.getMessage(), e);
            return createErrorResponse(AppConstants.ERROR_MESSAGE_REGISTRATION);
        }
    }

    /**
     * Register ADMIN to tenant_user table
     */
    private AuthResponse registerAdmin(RegisterRequest request, UserRole role) {
        try {
            Tenant tenant = tenantRepository.findActiveByTenantCode(request.getTenantCode()).get();

            String encryptedPassword = aesUtils.encrypt(request.getPassword());

            TenantUser tenantUser = new TenantUser();
            tenantUser.setTenantId(tenant.getTenantId());
            tenantUser.setPlatformUserId(1L); // Default platform user ID
            tenantUser.setRoleId(role.getRoleId());
            tenantUser.setTenantUserCode(codeGenerationService.generateTenantUserCode());
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

            log.info("ADMIN registration successful for email: {}", request.getEmail());

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
        } catch (Exception e) {
            log.error("Error registering ADMIN: {}", e.getMessage(), e);
            return createErrorResponse(AppConstants.ERROR_MESSAGE_REGISTRATION);
        }
    }

    /**
     * Register USER/MANAGER to users table
     */
    private AuthResponse registerUserOrManager(RegisterRequest request, UserRole role) {
        try {
            TenantUser adminUser = tenantUserRepository.findActiveByTenantUserCode(request.getTenantUserCode()).get();

            String encryptedPassword = aesUtils.encrypt(request.getPassword());

            User newUser = new User();
            newUser.setTenantUserId(adminUser.getTenantUserId());
            newUser.setName(request.getName());
            newUser.setEmail(request.getEmail());
            newUser.setMobileNumber(request.getMobileNumber());
            newUser.setPassword(encryptedPassword);
            newUser.setRoleId(role.getRoleId());
            newUser.setPasswordEncryptionKeyVersion(encryptionKeyService.getLatestKey().getKeyVersion());
            newUser.setIsActive(true);
            newUser.setLoginAttempts(0);
            newUser.setAccountLocked(false);

            User savedUser = userRepository.save(newUser);

            String jwtToken = jwtUtil.generateTokenWithUserDetails(
                    savedUser.getEmail(),
                    savedUser.getUserId(),
                    savedUser.getName(),
                    role.getRole()
            );

            log.info("{} registration successful for email: {}", request.getRole(), request.getEmail());

            return new AuthResponse(
                    jwtToken,
                    role.getRole(),
                    savedUser.getUserId(),
                    savedUser.getName(),
                    AppConstants.STATUS_SUCCESS,
                    AppConstants.SUCCESS_MESSAGE_REGISTRATION_SUCCESSFUL,
                    LocalDateTime.now(),
                    true,
                    0,
                    false
            );
        } catch (Exception e) {
            log.error("Error registering {}: {}", request.getRole(), e.getMessage(), e);
            return createErrorResponse(AppConstants.ERROR_MESSAGE_REGISTRATION);
        }
    }

    /**
     * Login using email + password (AES-encrypted password in DB).
     * Handles both tenant_user table (SUPER_ADMIN, ADMIN) and users table (USER, MANAGER) logins.
     */
    public AuthResponse login(LoginRequest request) {
        // First check in tenant_user table for SUPER_ADMIN/ADMIN
        Optional<TenantUser> tenantUserOpt = tenantUserRepository.findActiveByEmail(request.getEmail());
        if (tenantUserOpt.isPresent()) {
            return loginTenantUser(request, tenantUserOpt.get());
        }

        // Then check in users table for USER/MANAGER role
        Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());
        if (optionalUser.isEmpty()) {
            return createErrorResponse(AppConstants.ERROR_MESSAGE_USER_NOT_FOUND);
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
     * Login for regular users (USER, MANAGER role in users table)
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

        Optional<UserRole> roleOpt = userRoleRepository.findById(user.getRoleId());
        String roleName = roleOpt.map(UserRole::getRole).orElse("UNKNOWN");

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

            Optional<UserRole> roleOpt = userRoleRepository.findById(user.getRoleId());
            String roleName = roleOpt.map(UserRole::getRole).orElse("UNKNOWN");

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
     * Helper method to create error response
     */
    private AuthResponse createErrorResponse(String message) {
        return new AuthResponse(null, null, null, null,
                AppConstants.STATUS_FAILED, message, null, null, null, null);
    }

    /**
     * Helper method to check if string is blank
     */
    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
}
