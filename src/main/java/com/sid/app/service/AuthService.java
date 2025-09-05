package com.sid.app.service;

import com.sid.app.auth.JwtUtil;
import com.sid.app.constants.AppConstants;
import com.sid.app.entity.User;
import com.sid.app.model.AuthResponse;
import com.sid.app.model.LoginRequest;
import com.sid.app.model.RegisterRequest;
import com.sid.app.model.ForgotPasswordResetRequest;
import com.sid.app.model.ResponseDTO;
import com.sid.app.repository.UserRepository;
import com.sid.app.utils.AESUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AESUtils aesUtils;
    private final EncryptionKeyService encryptionKeyService;

    private static final ConcurrentHashMap<String, String> otpStore = new ConcurrentHashMap<>();

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
            String encryptedPassword = aesUtils.encrypt(request.getPassword());

            User newUser = new User();
            newUser.setName(request.getName());
            newUser.setEmail(request.getEmail());
            newUser.setMobileNumber(request.getMobileNumber());
            newUser.setPassword(encryptedPassword);
            newUser.setRole(request.getRole());
            newUser.setPasswordEncryptionKeyVersion(encryptionKeyService.getLatestKey().getKeyVersion());
            newUser.setIsActive(true);
            newUser.setLoginAttempts(0);
            newUser.setAccountLocked(false);

            User savedUser = userRepository.save(newUser);

            return new AuthResponse(
                    jwtUtil.generateToken(savedUser.getEmail()),
                    savedUser.getRole(),
                    savedUser.getUserId(),
                    savedUser.getName(),
                    AppConstants.STATUS_SUCCESS,
                    AppConstants.SUCCESS_MESSAGE_REGISTRATION_SUCCESSFUL,
                    null,
                    true,
                    0,
                    false
            );
        } catch (Exception e) {
            log.error("Error encrypting password: {}", e.getMessage());
            return new AuthResponse(null, null, null, null,
                    AppConstants.STATUS_FAILED,
                    AppConstants.ERROR_MESSAGE_REGISTRATION,
                    null, null, null, null);
        }
    }

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
                    null, user.getIsActive(), user.getLoginAttempts(), true);
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
            return new AuthResponse(null, null, null, null,
                    AppConstants.STATUS_FAILED,
                    AppConstants.ERROR_MESSAGE_LOGIN,
                    null, user.getIsActive(), user.getLoginAttempts(), user.getAccountLocked());
        }

        user.setLoginAttempts(0);
        user.setLastLoginTime(LocalDateTime.now());
        userRepository.save(user);

        return new AuthResponse(
                jwtUtil.generateToken(user.getEmail()),
                user.getRole(),
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
            return ResponseEntity.internalServerError()
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "Error resetting password.", null));
        }
    }

}
