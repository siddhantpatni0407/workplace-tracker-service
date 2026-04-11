package com.sid.app.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to change password for authenticated user.
 * UserId is automatically extracted from JWT token.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to change the authenticated user's password")
public class PasswordChangeRequest {

    @NotBlank(message = "currentPassword is required")
    @Size(min = 6, message = "currentPassword must be at least 6 characters")
    @Schema(description = "Current account password (min 6 characters)", example = "OldPass123!",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String currentPassword;

    @NotBlank(message = "newPassword is required")
    @Size(min = 8, message = "newPassword must be at least 8 characters")
    @Schema(description = "New password to set (min 8 characters)", example = "NewPass456!",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String newPassword;
}
