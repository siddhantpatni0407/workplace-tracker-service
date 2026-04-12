package com.sid.app.controller;

import com.sid.app.annotation.CorrelationId;
import com.sid.app.auth.RequiredRole;
import com.sid.app.constants.EndpointConstants;
import com.sid.app.enums.UserRole;
import com.sid.app.constants.AppConstants;
import com.sid.app.exception.InvalidEncryptionKeyException;
import com.sid.app.model.DecryptPasswordRequest;
import com.sid.app.model.DecryptPasswordResponse;
import com.sid.app.model.ResponseDTO;
import com.sid.app.service.SupportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Support APIs that are intended for administrators/support personnel.
 * <p>
 * NOTE: This controller exposes an endpoint that decrypts stored passwords. This is
 * highly sensitive and must be protected by role-based checks (ADMIN) and audit logs.
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Support", description = "Admin-only support APIs")
@SecurityRequirement(name = "bearerAuth")
public class SupportController {

    private final SupportService supportService;

    /**
     * Decrypt an encrypted password using the specified key version.
     * Only users with role ADMIN are authorized to call this API.
     * <p>
     * Request body: { "encryptedPassword": "...", "keyVersion": 1 }
     * Response: ResponseDTO with plaintext password as data.
     */
    @Operation(summary = "Decrypt password (ADMIN only)", description = "Decrypts an AES-encrypted password given the key version. Strictly admin-only.")
    @PostMapping(EndpointConstants.SUPPORT_DECRYPT_PASSWORD)
    @RequiredRole({UserRole.ADMIN})
    @CorrelationId
    public ResponseEntity<ResponseDTO<DecryptPasswordResponse>> decryptPassword(@Valid @RequestBody DecryptPasswordRequest req) {
        log.info("decryptPassword() - admin requested decryption for keyVersion={}", req.getKeyVersion());

        try {
            DecryptPasswordResponse dto = supportService.decryptPassword(req.getEncryptedPassword(), req.getKeyVersion());
            return ResponseEntity.ok(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, "Decryption successful", dto));
        } catch (InvalidEncryptionKeyException ex) {
            // Key for the requested version was not found or invalid — return 404 Not Found
            log.warn("decryptPassword() - encryption key not found for version {}: {}", req.getKeyVersion(), ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, ex.getMessage(), null));
        } catch (IllegalArgumentException ex) {
            log.warn("decryptPassword() - invalid request: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(new ResponseDTO<>(AppConstants.STATUS_FAILED, ex.getMessage(), null));
        } catch (Exception ex) {
            // Unexpected errors: log and return 500 without exposing internal stacktrace to clients
            log.error("decryptPassword() - decryption failed: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, AppConstants.ERROR_INTERNAL_SERVER, null));
        }
    }
}

