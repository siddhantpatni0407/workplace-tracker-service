package com.sid.app.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sid.app.model.ResponseDTO;
import com.sid.app.model.LoginRequest;
import com.sid.app.model.RegisterRequest;
import com.sid.app.model.PasswordChangeRequest;
import com.sid.app.model.ForgotPasswordResetRequest;
import com.sid.app.dto.request.PlatformUserLoginRequest;
import com.sid.app.dto.request.PlatformUserSignupRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ApplicationUtils {

    private static final String MASKED_PASSWORD = "****";

    /**
     * Gets json string.
     *
     * @param <T>    the type parameter
     * @param object the object
     * @return the json string
     */
    public static <T> String getJSONString(T object) {
        if (object != null) {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule()); // ✅ Enables LocalDate serialization
            try {
                return objectMapper.writeValueAsString(object);
            } catch (Exception e) {
                if (log.isErrorEnabled()) {
                    log.error("Error occurred [{}] while converting to string [{}]", e.getMessage(), object);
                }
            }
        }
        return "";
    }

    public static <T> T readValue(String content, Class<T> valueType) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(content, valueType);
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Error occurred [{}] while reading from string [{}]", e.getMessage(), content);
            }
        }
        return null;
    }

    /**
     * Builds a response with dynamic status and message.
     *
     * @param data    The response data (nullable)
     * @param message Response message
     * @param status  HTTP status code
     * @param <T>     Type of the response data
     * @return ResponseEntity with standardized response format
     */
    public static <T> ResponseDTO<T> buildResponse(T data, String message, String status) {
        return ResponseDTO.<T>builder()
                .status(status)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * Creates a secure JSON string for LoginRequest with masked password
     *
     * @param request the login request
     * @return JSON string with masked password
     */
    public static String getSecureJSONString(LoginRequest request) {
        if (request == null) {
            return "";
        }

        try {
            // Create a copy with masked password for logging
            LoginRequest secureRequest = new LoginRequest();
            secureRequest.setEmail(request.getEmail());
            secureRequest.setPassword(MASKED_PASSWORD);

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            return objectMapper.writeValueAsString(secureRequest);
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Error occurred [{}] while converting LoginRequest to secure string", e.getMessage());
            }
            return "LoginRequest{email=" + request.getEmail() + ", password=" + MASKED_PASSWORD + "}";
        }
    }

    /**
     * Creates a secure JSON string for RegisterRequest with masked password
     *
     * @param request the register request
     * @return JSON string with masked password
     */
    public static String getSecureJSONString(RegisterRequest request) {
        if (request == null) {
            return "";
        }

        try {
            // Create a copy with masked password for logging
            RegisterRequest secureRequest = new RegisterRequest();
            secureRequest.setName(request.getName());
            secureRequest.setMobileNumber(request.getMobileNumber());
            secureRequest.setEmail(request.getEmail());
            secureRequest.setPassword(MASKED_PASSWORD);
            secureRequest.setRole(request.getRole());
            secureRequest.setPlatformUserCode(request.getPlatformUserCode());
            secureRequest.setTenantCode(request.getTenantCode());
            secureRequest.setTenantUserCode(request.getTenantUserCode());

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            return objectMapper.writeValueAsString(secureRequest);
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Error occurred [{}] while converting RegisterRequest to secure string", e.getMessage());
            }
            return "RegisterRequest{email=" + request.getEmail() + ", password=" + MASKED_PASSWORD + ", role=" + request.getRole() + "}";
        }
    }

    /**
     * Creates a secure JSON string for PasswordChangeRequest with masked passwords
     *
     * @param request the password change request
     * @return JSON string with masked passwords
     */
    public static String getSecureJSONString(PasswordChangeRequest request) {
        if (request == null) {
            return "";
        }

        try {
            // Create a copy with masked passwords for logging
            PasswordChangeRequest secureRequest = new PasswordChangeRequest();
            secureRequest.setCurrentPassword(MASKED_PASSWORD);
            secureRequest.setNewPassword(MASKED_PASSWORD);

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            return objectMapper.writeValueAsString(secureRequest);
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Error occurred [{}] while converting PasswordChangeRequest to secure string", e.getMessage());
            }
            return "PasswordChangeRequest{currentPassword=" + MASKED_PASSWORD + ", newPassword=" + MASKED_PASSWORD + "}";
        }
    }

    /**
     * Creates a secure JSON string for ForgotPasswordResetRequest with masked password
     *
     * @param request the forgot password reset request
     * @return JSON string with masked password
     */
    public static String getSecureJSONString(ForgotPasswordResetRequest request) {
        if (request == null) {
            return "";
        }

        try {
            // Create a copy with masked password for logging
            ForgotPasswordResetRequest secureRequest = new ForgotPasswordResetRequest();
            secureRequest.setEmail(request.getEmail());
            secureRequest.setOtp(request.getOtp());
            secureRequest.setNewPassword(MASKED_PASSWORD);

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            return objectMapper.writeValueAsString(secureRequest);
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Error occurred [{}] while converting ForgotPasswordResetRequest to secure string", e.getMessage());
            }
            return "ForgotPasswordResetRequest{email=" + request.getEmail() + ", otp=" + request.getOtp() + ", newPassword=" + MASKED_PASSWORD + "}";
        }
    }

    /**
     * Creates a secure JSON string for PlatformUserLoginRequest with masked password
     *
     * @param request the platform user login request
     * @return JSON string with masked password
     */
    public static String getSecureJSONString(PlatformUserLoginRequest request) {
        if (request == null) {
            return "";
        }

        try {
            // Create a copy with masked password for logging
            PlatformUserLoginRequest secureRequest = new PlatformUserLoginRequest();
            secureRequest.setEmailOrMobile(request.getEmailOrMobile());
            secureRequest.setPassword(MASKED_PASSWORD);

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            return objectMapper.writeValueAsString(secureRequest);
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Error occurred [{}] while converting PlatformUserLoginRequest to secure string", e.getMessage());
            }
            return "PlatformUserLoginRequest{emailOrMobile=" + request.getEmailOrMobile() + ", password=" + MASKED_PASSWORD + "}";
        }
    }

    /**
     * Creates a secure JSON string for PlatformUserSignupRequest with masked passwords
     *
     * @param request the platform user signup request
     * @return JSON string with masked passwords
     */
    public static String getSecureJSONString(PlatformUserSignupRequest request) {
        if (request == null) {
            return "";
        }

        try {
            // Create a copy with masked passwords for logging
            PlatformUserSignupRequest secureRequest = new PlatformUserSignupRequest();
            secureRequest.setName(request.getName());
            secureRequest.setEmail(request.getEmail());
            secureRequest.setMobileNumber(request.getMobileNumber());
            secureRequest.setPassword(MASKED_PASSWORD);
            secureRequest.setConfirmPassword(MASKED_PASSWORD);

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            return objectMapper.writeValueAsString(secureRequest);
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Error occurred [{}] while converting PlatformUserSignupRequest to secure string", e.getMessage());
            }
            return "PlatformUserSignupRequest{name=" + request.getName() + ", email=" + request.getEmail() + ", password=" + MASKED_PASSWORD + ", confirmPassword=" + MASKED_PASSWORD + "}";
        }
    }

}