package com.sid.app.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author Siddhant Patni
 */
@Data
@Schema(description = "User login credentials")
public class LoginRequest {

    @Schema(description = "Registered email address", example = "john.doe@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @Schema(description = "Account password", example = "SecurePass123!",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

}