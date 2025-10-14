package com.sid.app.service.impl;

import com.sid.app.auth.JwtUtil;
import com.sid.app.constants.AppConstants;
import com.sid.app.dto.request.PlatformUserLoginRequest;
import com.sid.app.dto.request.PlatformUserSignupRequest;
import com.sid.app.dto.response.PlatformUserAuthResponse;
import com.sid.app.dto.response.PlatformUserResponse;
import com.sid.app.entity.PlatformUser;
import com.sid.app.repository.PlatformUserRepository;
import com.sid.app.service.EncryptionKeyService;
import com.sid.app.service.PlatformUserService;
import com.sid.app.service.CodeGenerationService;
import com.sid.app.utils.AESUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlatformUserServiceImpl implements PlatformUserService {

    private final PlatformUserRepository platformUserRepository;
    private final JwtUtil jwtUtil;
    private final AESUtils aesUtils;
    private final EncryptionKeyService encryptionKeyService;
    private final CodeGenerationService codeGenerationService;

    @Override
    @Transactional
    public PlatformUserAuthResponse signup(PlatformUserSignupRequest request) {
        try {
            log.info("Processing signup request for email: {}", request.getEmail());

            // Check if user already exists by email
            if (platformUserRepository.existsByEmail(request.getEmail())) {
                return buildFailureResponse(AppConstants.ERROR_MESSAGE_EMAIL_EXISTS);
            }

            // Check if user already exists by mobile number
            if (request.getMobileNumber() != null &&
                platformUserRepository.existsByMobileNumber(request.getMobileNumber())) {
                return buildFailureResponse(AppConstants.ERROR_MESSAGE_MOBILE_EXISTS);
            }

            // Encrypt password using AES utils (following AuthService pattern)
            String encryptedPassword = aesUtils.encrypt(request.getPassword());

            // Generate unique platform user code
            String platformUserCode;
            do {
                platformUserCode = codeGenerationService.generatePlatformUserCode();
            } while (platformUserRepository.existsByPlatformUserCode(platformUserCode));

            // Create new platform user
            PlatformUser user = new PlatformUser();
            user.setName(request.getName());
            user.setEmail(request.getEmail());
            user.setPlatformUserCode(platformUserCode);
            user.setMobileNumber(request.getMobileNumber());
            user.setPassword(encryptedPassword);
            user.setRoleId(1L); // Default platform user role ID
            user.setPasswordEncryptionKeyVersion(encryptionKeyService.getLatestKey().getKeyVersion());
            user.setIsActive(Boolean.TRUE); // Platform users are active by default
            user.setLoginAttempts(0);
            user.setAccountLocked(Boolean.FALSE);

            user = platformUserRepository.save(user);

            // Generate JWT token with user details
            String token = jwtUtil.generateTokenWithUserDetails(
                    user.getEmail(),
                    user.getPlatformUserId(),
                    user.getName(),
                    "PLATFORM_USER"
            );

            // Generate refresh token with longer TTL
            long refreshTtlMs = 7L * 24L * 60L * 60L * 1000L; // 7 days
            String refreshToken = jwtUtil.generateToken(user.getEmail(), null, refreshTtlMs);

            log.info("Platform user signup successful for email: {}", request.getEmail());

            return PlatformUserAuthResponse.builder()
                    .token(token)
                    .refreshToken(refreshToken)
                    .role("PLATFORM_USER")
                    .platformUserId(user.getPlatformUserId())
                    .name(user.getName())
                    .status(AppConstants.STATUS_SUCCESS)
                    .message(AppConstants.SUCCESS_MESSAGE_REGISTRATION_SUCCESSFUL)
                    .lastLoginTime(LocalDateTime.now())
                    .isActive(user.getIsActive())
                    .loginAttempts(user.getLoginAttempts())
                    .accountLocked(user.getAccountLocked())
                    .build();

        } catch (Exception e) {
            log.error("Error during platform user signup: {}", e.getMessage(), e);
            return buildFailureResponse(AppConstants.ERROR_MESSAGE_REGISTRATION);
        }
    }

    @Override
    @Transactional
    public PlatformUserAuthResponse login(PlatformUserLoginRequest request) {
        try {
            log.info("Processing login request for: {}", request.getEmailOrMobile());

            // Find user by email or mobile
            Optional<PlatformUser> userOpt = platformUserRepository.findByEmail(request.getEmailOrMobile())
                    .or(() -> platformUserRepository.findByMobileNumber(request.getEmailOrMobile()));

            if (userOpt.isEmpty()) {
                return buildFailureResponse(AppConstants.ERROR_MESSAGE_USER_NOT_FOUND);
            }

            PlatformUser user = userOpt.get();
            LocalDateTime previousLoginTime = user.getLastLoginTime();

            // Check if user is active
            if (!user.getIsActive()) {
                return buildFailureResponse(AppConstants.ERROR_MESSAGE_INACTIVE_ACCOUNT);
            }

            // Check if account is locked
            if (Boolean.TRUE.equals(user.getAccountLocked())) {
                return buildFailureResponse(AppConstants.ERROR_MESSAGE_ACCOUNT_LOCKED);
            }

            try {
                // Decrypt password using AES utils with versioning (following AuthService pattern)
                String decryptedPassword = aesUtils.decrypt(user.getPassword(),
                        user.getPasswordEncryptionKeyVersion());

                if (!request.getPassword().equals(decryptedPassword)) {
                    // Increment login attempts
                    user.setLoginAttempts(user.getLoginAttempts() + 1);

                    // Lock account after 5 failed attempts
                    if (user.getLoginAttempts() >= 5) {
                        user.setAccountLocked(Boolean.TRUE);
                    }
                    platformUserRepository.save(user);

                    return buildFailureResponse(AppConstants.ERROR_MESSAGE_INVALID_LOGIN);
                }
            } catch (Exception e) {
                log.error("Error decrypting password for user {}: {}", request.getEmailOrMobile(), e.getMessage());
                return buildFailureResponse(AppConstants.ERROR_MESSAGE_LOGIN);
            }

            // Reset login attempts on successful login and update last login time (following AuthService pattern)
            user.setLoginAttempts(0);
            user.setLastLoginTime(LocalDateTime.now());
            platformUserRepository.save(user);

            // Generate JWT token with user details
            String token = jwtUtil.generateTokenWithUserDetails(
                    user.getEmail(),
                    user.getPlatformUserId(),
                    user.getName(),
                    "PLATFORM_USER"
            );

            // Generate refresh token with longer TTL
            long refreshTtlMs = 7L * 24L * 60L * 60L * 1000L; // 7 days
            String refreshToken = jwtUtil.generateToken(user.getEmail(), null, refreshTtlMs);

            log.info("Platform user login successful for: {}", request.getEmailOrMobile());

            return PlatformUserAuthResponse.builder()
                    .token(token)
                    .refreshToken(refreshToken)
                    .role("PLATFORM_USER")
                    .platformUserId(user.getPlatformUserId())
                    .name(user.getName())
                    .status(AppConstants.STATUS_SUCCESS)
                    .message(AppConstants.LOGIN_SUCCESSFUL_MESSAGE)
                    .lastLoginTime(user.getLastLoginTime()) // Return current login time, not previous
                    .isActive(user.getIsActive())
                    .loginAttempts(user.getLoginAttempts())
                    .accountLocked(user.getAccountLocked())
                    .build();

        } catch (Exception e) {
            log.error("Error during platform user login: {}", e.getMessage(), e);
            return buildFailureResponse(AppConstants.ERROR_MESSAGE_LOGIN);
        }
    }

    @Override
    public PlatformUserAuthResponse refreshToken(String refreshToken) {
        try {
            log.info("Processing token refresh request");

            if (refreshToken == null || refreshToken.isBlank()) {
                return buildFailureResponse("Missing refresh token.");
            }

            // Validate refresh token and extract username (will throw ExpiredJwtException if expired)
            String email = jwtUtil.extractUsername(refreshToken);

            // Find user by email
            Optional<PlatformUser> userOpt = platformUserRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                return buildFailureResponse(AppConstants.ERROR_MESSAGE_USER_NOT_FOUND);
            }

            PlatformUser user = userOpt.get();

            // Check if user is active
            if (!user.getIsActive()) {
                return buildFailureResponse(AppConstants.ERROR_MESSAGE_INACTIVE_ACCOUNT);
            }

            // Check if account is locked
            if (Boolean.TRUE.equals(user.getAccountLocked())) {
                return buildFailureResponse(AppConstants.ERROR_MESSAGE_ACCOUNT_LOCKED);
            }

            // Generate new access token with user details
            String newToken = jwtUtil.generateTokenWithUserDetails(
                    user.getEmail(),
                    user.getPlatformUserId(),
                    user.getName(),
                    "PLATFORM_USER"
            );

            // Generate new refresh token with longer TTL
            long refreshTtlMs = 7L * 24L * 60L * 60L * 1000L; // 7 days
            String newRefreshToken = jwtUtil.generateToken(user.getEmail(), null, refreshTtlMs);

            return PlatformUserAuthResponse.builder()
                    .token(newToken)
                    .refreshToken(newRefreshToken)
                    .role("PLATFORM_USER")
                    .platformUserId(user.getPlatformUserId())
                    .name(user.getName())
                    .status(AppConstants.STATUS_SUCCESS)
                    .message("Token refreshed successfully")
                    .isActive(user.getIsActive())
                    .loginAttempts(user.getLoginAttempts())
                    .accountLocked(user.getAccountLocked())
                    .build();

        } catch (Exception e) {
            log.error("Error during platform user token refresh: {}", e.getMessage(), e);
            return buildFailureResponse("Token refresh failed: " + e.getMessage());
        }
    }

    @Override
    public PlatformUserResponse getPlatformUserProfile(Long platformUserId) {
        try {
            log.info("Fetching profile for platform user ID: {}", platformUserId);

            Optional<PlatformUser> userOpt = platformUserRepository.findById(platformUserId);
            if (userOpt.isEmpty()) {
                throw new RuntimeException("Platform user not found");
            }

            PlatformUser user = userOpt.get();

            return PlatformUserResponse.builder()
                    .platformUserId(user.getPlatformUserId())
                    .name(user.getName())
                    .email(user.getEmail())
                    .mobileNumber(user.getMobileNumber())
                    .isActive(user.getIsActive())
                    .accountLocked(user.getAccountLocked())
                    .loginAttempts(user.getLoginAttempts())
                    .createdDate(user.getCreatedDate())
                    .modifiedDate(user.getModifiedDate())
                    .build();

        } catch (Exception e) {
            log.error("Error fetching platform user profile: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch user profile: " + e.getMessage());
        }
    }

    private PlatformUserAuthResponse buildFailureResponse(String message) {
        return PlatformUserAuthResponse.builder()
                .token(null)
                .refreshToken(null)
                .role(null)
                .platformUserId(null)
                .name(null)
                .status(AppConstants.STATUS_FAILED)
                .message(message)
                .lastLoginTime(null)
                .isActive(null)
                .loginAttempts(null)
                .accountLocked(null)
                .build();
    }
}
