package com.sid.app.service.impl;

import com.sid.app.auth.JwtUtil;
import com.sid.app.constants.AppConstants;
import com.sid.app.entity.Tenant;
import com.sid.app.entity.TenantUser;
import com.sid.app.entity.User;
import com.sid.app.entity.UserRole;
import com.sid.app.model.AuthResponse;
import com.sid.app.model.LoginRequest;
import com.sid.app.model.RegisterRequest;
import com.sid.app.repository.TenantRepository;
import com.sid.app.repository.TenantUserRepository;
import com.sid.app.repository.UserRepository;
import com.sid.app.repository.UserRoleRepository;
import com.sid.app.service.EncryptionKeyService;
import com.sid.app.service.TenantAuthService;
import com.sid.app.utils.AESUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Implementation of TenantAuthService for handling tenant-based user authentication
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TenantAuthServiceImpl implements TenantAuthService {

    private final TenantRepository tenantRepository;
    private final TenantUserRepository tenantUserRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final JwtUtil jwtUtil;
    private final AESUtils aesUtils;
    private final EncryptionKeyService encryptionKeyService;

    @Override
    @Transactional
    public AuthResponse registerTenantUser(RegisterRequest request) {
        try {
            log.info("Processing tenant user registration for email: {} with role: {}",
                    request.getEmail(), request.getRole());

            // Validate tenant code is required for these roles
            if (isNullOrEmpty(request.getTenantCode())) {
                return buildFailureResponse("Tenant code is required for " + request.getRole() + " registration");
            }

            // Validate role
            if (!isValidTenantRole(request.getRole())) {
                return buildFailureResponse("Invalid role. Only SUPER_ADMIN, ADMIN, and USER roles are supported");
            }

            // Validate tenant exists and is active
            Optional<Tenant> tenantOpt = tenantRepository.findActiveByTenantCode(request.getTenantCode());
            if (tenantOpt.isEmpty()) {
                return buildFailureResponse("Invalid or inactive tenant code: " + request.getTenantCode());
            }

            Tenant tenant = tenantOpt.get();

            // Get role ID
            Optional<UserRole> roleOpt = userRoleRepository.findByRoleIgnoreCase(request.getRole());
            if (roleOpt.isEmpty()) {
                return buildFailureResponse("Role not found: " + request.getRole());
            }

            UserRole role = roleOpt.get();

            // Check for existing users
            if (tenantUserRepository.existsByEmail(request.getEmail())) {
                return buildFailureResponse(AppConstants.ERROR_MESSAGE_EMAIL_EXISTS);
            }

            if (request.getMobileNumber() != null && tenantUserRepository.existsByMobileNumber(request.getMobileNumber())) {
                return buildFailureResponse(AppConstants.ERROR_MESSAGE_MOBILE_EXISTS);
            }

            // For USER role, also check users table
            if ("USER".equalsIgnoreCase(request.getRole())) {
                if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                    return buildFailureResponse(AppConstants.ERROR_MESSAGE_EMAIL_EXISTS);
                }
            }

            // Encrypt password
            String encryptedPassword = aesUtils.encrypt(request.getPassword());

            if ("SUPER_ADMIN".equalsIgnoreCase(request.getRole()) || "ADMIN".equalsIgnoreCase(request.getRole())) {
                return registerTenantUserOnly(request, tenant, role, encryptedPassword);
            } else if ("USER".equalsIgnoreCase(request.getRole())) {
                return registerUserWithTenantUser(request, tenant, role, encryptedPassword);
            }

            return buildFailureResponse("Unsupported role for tenant registration: " + request.getRole());

        } catch (Exception e) {
            log.error("Error during tenant user registration: {}", e.getMessage(), e);
            return buildFailureResponse(AppConstants.ERROR_MESSAGE_REGISTRATION);
        }
    }

    @Override
    public AuthResponse loginTenantUser(LoginRequest request) {
        try {
            log.info("Processing tenant user login for email: {}", request.getEmail());

            // First check in tenant_user table
            Optional<TenantUser> tenantUserOpt = tenantUserRepository.findActiveByEmail(request.getEmail());
            if (tenantUserOpt.isPresent()) {
                return loginTenantUserEntity(request, tenantUserOpt.get());
            }

            // Then check in users table for USER role
            Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (user.getIsActive()) {
                    return loginUserEntity(request, user);
                }
            }

            return buildFailureResponse(AppConstants.ERROR_MESSAGE_USER_NOT_FOUND);

        } catch (Exception e) {
            log.error("Error during tenant user login: {}", e.getMessage(), e);
            return buildFailureResponse(AppConstants.ERROR_MESSAGE_LOGIN);
        }
    }

    private AuthResponse registerTenantUserOnly(RegisterRequest request, Tenant tenant, UserRole role, String encryptedPassword) {
        // Check if SUPER_ADMIN already exists for this tenant
        if ("SUPER_ADMIN".equalsIgnoreCase(request.getRole())) {
            if (!tenantUserRepository.findByTenantIdAndRoleId(tenant.getTenantId(), role.getRoleId()).isEmpty()) {
                return buildFailureResponse("A SUPER_ADMIN already exists for this tenant");
            }
        }

        TenantUser tenantUser = new TenantUser();
        tenantUser.setTenantId(tenant.getTenantId());
        tenantUser.setPlatformUserId(1L); // Default platform user ID - you may need to adjust this
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

    private AuthResponse registerUserWithTenantUser(RegisterRequest request, Tenant tenant, UserRole role, String encryptedPassword) {
        // First create tenant_user entry
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

        // Then create user entry referencing tenant_user
        User user = new User();
        user.setTenantUserId(tenantUser.getTenantUserId());
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setMobileNumber(request.getMobileNumber());
        user.setPassword(encryptedPassword);
        user.setPasswordEncryptionKeyVersion(encryptionKeyService.getLatestKey().getKeyVersion());
        user.setRoleId(role.getRoleId());
        user.setIsActive(true);
        user.setLoginAttempts(0);
        user.setAccountLocked(false);

        user = userRepository.save(user);

        String jwtToken = jwtUtil.generateTokenWithUserDetails(
                user.getEmail(),
                user.getUserId(),
                user.getName(),
                role.getRole()
        );

        log.info("User registration successful for email: {} with role: {}", request.getEmail(), request.getRole());

        return new AuthResponse(
                jwtToken,
                role.getRole(),
                user.getUserId(),
                user.getName(),
                AppConstants.STATUS_SUCCESS,
                AppConstants.SUCCESS_MESSAGE_REGISTRATION_SUCCESSFUL,
                LocalDateTime.now(),
                true,
                0,
                false
        );
    }

    private AuthResponse loginTenantUserEntity(LoginRequest request, TenantUser tenantUser) {
        if (!tenantUser.getIsActive()) {
            return buildFailureResponse(AppConstants.ERROR_MESSAGE_INACTIVE_ACCOUNT);
        }

        if (Boolean.TRUE.equals(tenantUser.getAccountLocked())) {
            return buildFailureResponse(AppConstants.ERROR_MESSAGE_ACCOUNT_LOCKED);
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
                return buildFailureResponse(AppConstants.ERROR_MESSAGE_INVALID_LOGIN);
            }
        } catch (Exception e) {
            log.error("Error decrypting password for tenant user {}: {}", request.getEmail(), e.getMessage());
            return buildFailureResponse(AppConstants.ERROR_MESSAGE_LOGIN);
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

    private AuthResponse loginUserEntity(LoginRequest request, User user) {
        if (!user.getIsActive()) {
            return buildFailureResponse(AppConstants.ERROR_MESSAGE_INACTIVE_ACCOUNT);
        }

        if (Boolean.TRUE.equals(user.getAccountLocked())) {
            return buildFailureResponse(AppConstants.ERROR_MESSAGE_ACCOUNT_LOCKED);
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
                return buildFailureResponse(AppConstants.ERROR_MESSAGE_INVALID_LOGIN);
            }
        } catch (Exception e) {
            log.error("Error decrypting password for user {}: {}", request.getEmail(), e.getMessage());
            return buildFailureResponse(AppConstants.ERROR_MESSAGE_LOGIN);
        }

        // Update last login time and reset login attempts
        user.setLoginAttempts(0);
        user.setLastLoginTime(LocalDateTime.now());
        userRepository.save(user);

        Optional<UserRole> roleOpt = userRoleRepository.findById(user.getRoleId());
        String roleName = roleOpt.map(UserRole::getRole).orElse("UNKNOWN");

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

    private boolean isValidTenantRole(String role) {
        return "SUPER_ADMIN".equalsIgnoreCase(role) ||
               "ADMIN".equalsIgnoreCase(role) ||
               "USER".equalsIgnoreCase(role) ||
               "MANAGER".equalsIgnoreCase(role);
    }

    private boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    private AuthResponse buildFailureResponse(String message) {
        return new AuthResponse(null, null, null, null,
                AppConstants.STATUS_FAILED, message, null, null, null, null);
    }
}
