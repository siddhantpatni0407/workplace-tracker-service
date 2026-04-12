package com.sid.app.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for decrypted password returned by support APIs.
 * NOTE: This contains plaintext password and must be handled carefully by callers.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Decrypted password response returned to ADMIN callers. Contains plaintext and encrypted values; handle with care")
public class DecryptPasswordResponse {

    @Schema(description = "The original encrypted password that was decrypted (base64 string).", example = "QmFzZTY0RW5jb2RlZEluU3RyaW5n")
    private String encryptedPassword;

    @Schema(description = "The plaintext (decrypted) password. This is sensitive data and should not be logged or persisted.", example = "P@ssw0rd123")
    private String decryptedPassword;

    @Schema(description = "Encryption key version used to decrypt the password.", example = "1")
    private Integer keyVersion;
}

