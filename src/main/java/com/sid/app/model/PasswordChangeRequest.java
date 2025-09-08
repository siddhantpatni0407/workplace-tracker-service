package com.sid.app.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to change password.
 * - userId is optional for admin flows; in typical user flow you should derive userId from JWT.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordChangeRequest {

    /**
     * Optional user id. If omitted, server should derive user id from auth token (recommended).
     */
    private Long userId;

    @NotBlank(message = "currentPassword is required")
    @Size(min = 6, message = "currentPassword must be at least 6 characters")
    private String currentPassword;

    @NotBlank(message = "newPassword is required")
    @Size(min = 8, message = "newPassword must be at least 8 characters")
    private String newPassword;
}
