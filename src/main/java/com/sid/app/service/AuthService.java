package com.sid.app.service;

import com.sid.app.auth.JwtUtil;
import com.sid.app.constants.AppConstants;
import com.sid.app.entity.User;
import com.sid.app.entity.UserRole;
import com.sid.app.exception.UserNotFoundException;
import com.sid.app.model.AuthResponse;
import com.sid.app.model.LoginRequest;
import com.sid.app.model.RegisterRequest;
import com.sid.app.model.ForgotPasswordResetRequest;
import com.sid.app.model.ResponseDTO;
import com.sid.app.repository.UserRepository;
import com.sid.app.repository.UserRoleRepository;
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
 * Updated to use role_id on User and resolve role names via UserRoleRepository.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AESUtils aesUtils;
    private final EncryptionKeyService encryptionKeyService;

    private static final ConcurrentHashMap<String, String> otpStore = new ConcurrentHashMap<>();

    /**
     * Register new user. Expects RegisterRequest to contain role (string) optionally.
     * Maps role name -> role_id and stores role_id on User.
     */
    public AuthResponse register(RegisterRequest request) {
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

            return new AuthResponse(
                    jwtUtil.generateToken(savedUser.getEmail()),
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
     */
    public AuthResponse login(LoginRequest request) {
        Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());

        if (optionalUser.isEmpty()) {
            return new AuthResponse(null, null, null, null,
                    AppConstants.STATUS_FAILED,
                    AppConstants.ERROR_MESSAGE_USER_NOT_FOUND,
                    null, null, null, null);
        }

        User user = optionalUser.get();
        LocalDateTime previousLoginTime = user.getLastLoginTime();

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

        user.setLoginAttempts(0);
        user.setLastLoginTime(LocalDateTime.now());
        userRepository.save(user);

        String roleName = resolveRoleName(user.getRoleId());

        return new AuthResponse(
                jwtUtil.generateToken(user.getEmail()),
                roleName,
                user.getUserId(),
                user.getName(),
                AppConstants.STATUS_SUCCESS,
                AppConstants.LOGIN_SUCCESSFUL_MESSAGE,
                previousLoginTime,
                true,
                0,
                false
        );
    }

    /**
     * Create and attach a refresh token cookie for the given user's email.
     *
     * @param email           user's email (subject for token)
     * @param servletResponse HttpServletResponse to set cookie header
     */
    public void createRefreshCookieForUser(String email, HttpServletResponse servletResponse) {
        // TTL for refresh token: 7 days (adjust as needed)
        long refreshTtlMs = 7L * 24L * 60L * 60L * 1000L;

        // Generate refresh token (stateless JWT using JwtUtil generateToken with TTL override)
        String refreshToken = jwtUtil.generateToken(email, null, refreshTtlMs);

        // Build HttpOnly cookie. Set secure=true in production (requires HTTPS).
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false) // <-- set to true in production
                .path("/")
                .maxAge(Duration.ofMillis(refreshTtlMs))
                .sameSite("Lax")
                .build();

        servletResponse.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    /**
     * Reset password using OTP (simple flow using in-memory otpStore - adapt as needed).
     */
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

    /**
     * Validate provided refresh token, issue a fresh access token and rotate refresh token.
     * Returns AuthResponse with new access token in token field.
     */
    public AuthResponse refreshToken(String refreshToken, HttpServletResponse servletResponse) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return new AuthResponse(null, null, null, null,
                    AppConstants.STATUS_FAILED, "Missing refresh token.", null, null, null, null);
        }

        try {
            // Validate refresh token and extract username (will throw ExpiredJwtException if expired)
            String username = jwtUtil.extractUsername(refreshToken);

            // Ensure the user still exists and is active
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

            // Issue a fresh access token (default TTL via JwtUtil)
            String newAccessToken = jwtUtil.generateToken(user.getEmail());

            // Rotate refresh token: create a new refresh token with longer TTL (e.g., 7 days)
            long refreshTtlMs = 7L * 24L * 60L * 60L * 1000L; // 7 days
            String newRefreshToken = jwtUtil.generateToken(user.getEmail(), null, refreshTtlMs);

            // Build secure HttpOnly cookie for refresh token
            ResponseCookie cookie = ResponseCookie.from("refreshToken", newRefreshToken)
                    .httpOnly(true)
                    .secure(false) // set to true in production (when using HTTPS)
                    .path("/")
                    .maxAge(Duration.ofMillis(refreshTtlMs))
                    .sameSite("Lax")
                    .build();

            servletResponse.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());

            String roleName = resolveRoleName(user.getRoleId());

            // prepare response (reuse your AuthResponse constructor/semantics)
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
        } catch (io.jsonwebtoken.ExpiredJwtException eje) {
            log.debug("Refresh token expired: {}", eje.getMessage());
            return new AuthResponse(null, null, null, null,
                    AppConstants.STATUS_FAILED, "Refresh token expired.", null, null, null, null);
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
    // Helper methods - role mapping
    // ---------------------------

    /**
     * Resolve role name from DB by roleId. Returns "USER" as fallback if not found or null.
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
     * Resolve roleId by role name. If roleName is null/blank, defaults to "USER".
     * Throws IllegalArgumentException if role name is not found in DB.
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
