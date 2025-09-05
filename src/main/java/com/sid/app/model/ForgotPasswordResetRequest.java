package com.sid.app.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * * DTO for Forgot Password Reset Request
 *
 * @author Siddhant Patni
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordResetRequest {

    private String email;
    private String otp;
    private String newPassword;

}