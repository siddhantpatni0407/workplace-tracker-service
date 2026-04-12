package com.sid.app.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for support API to decrypt a password.
 * Only administrators should be allowed to use this API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DecryptPasswordRequest {

    @NotBlank(message = "encryptedPassword must be provided")
    private String encryptedPassword;

    @NotNull(message = "keyVersion must be provided")
    private Integer keyVersion;
}

