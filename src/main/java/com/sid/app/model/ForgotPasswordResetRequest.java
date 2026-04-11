package com.sid.app.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Forgot Password Reset Request
 *
 * @author Siddhant Patni
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to reset a forgotten password using an OTP")
public class ForgotPasswordResetRequest {

    @Schema(description = "Email address associated with the account", example = "john.doe@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @Schema(description = "One-time password sent to the registered email address", example = "482931",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String otp;

    @Schema(description = "New password to set (minimum 8 characters)", example = "NewSecurePass789!",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String newPassword;

}