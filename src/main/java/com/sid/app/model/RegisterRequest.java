package com.sid.app.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author Siddhant Patni
 */
@Data
@Schema(description = "User registration request payload")
public class RegisterRequest {

    @JsonProperty("name")
    @Schema(description = "Full name of the user", example = "John Doe",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @JsonProperty("mobileNumber")
    @Schema(description = "Mobile phone number", example = "+1234567890")
    private String mobileNumber;

    @JsonProperty("email")
    @Schema(description = "Email address (must be unique)", example = "john.doe@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @JsonProperty("password")
    @Schema(description = "Password — minimum 8 characters", example = "SecurePass123!",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @JsonProperty("role")
    @Schema(description = "User role: SUPER_ADMIN | ADMIN | MANAGER | USER", example = "USER",
            allowableValues = {"SUPER_ADMIN", "ADMIN", "MANAGER", "USER"},
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String role;

    @JsonProperty("platformUserCode")
    @Schema(description = "Required only for SUPER_ADMIN registration", example = "PU-ABC123")
    private String platformUserCode;

    @JsonProperty("tenantCode")
    @Schema(description = "Required for SUPER_ADMIN registration", example = "TNT-XYZ789")
    private String tenantCode;

    @JsonProperty("tenantUserCode")
    @Schema(description = "Required for ADMIN, MANAGER and USER registration", example = "TU-DEF456")
    private String tenantUserCode;

}