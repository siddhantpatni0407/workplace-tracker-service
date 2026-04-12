package com.sid.app.service;

import com.sid.app.annotation.CorrelationId;
import com.sid.app.exception.InvalidEncryptionKeyException;
import com.sid.app.model.DecryptPasswordResponse;
import com.sid.app.utils.AESUtils;
import com.sid.app.auth.JwtAuthenticationContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service layer for support operations.
 * Contains the decryption logic and audit logging so controller remains thin.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SupportService {

    private final AESUtils aesUtils;
    private final JwtAuthenticationContext authContext;

    /**
     * Decrypts the provided encrypted password using the specified key version.
     * Performs audit logging about who requested the operation.
     *
     * @param encryptedPassword base64 AES-encrypted password
     * @param keyVersion        key version to use for decryption
     * @return plaintext password
     * @throws InvalidEncryptionKeyException if the key version is not found or invalid
     */
    @CorrelationId
    public DecryptPasswordResponse decryptPassword(String encryptedPassword, Integer keyVersion) {
        Long requesterId = authContext.getCurrentUserId();
        String requesterEmail = authContext.getCurrentUserEmail();

        // Validate input early to return clear 400 errors for bad requests
        if (encryptedPassword == null || encryptedPassword.isBlank()) {
            log.warn("SupportService.decryptPassword() - missing encryptedPassword from userId={}", requesterId);
            throw new IllegalArgumentException("encryptedPassword must be provided");
        }

        if (keyVersion == null || keyVersion <= 0) {
            log.warn("SupportService.decryptPassword() - invalid keyVersion={} from userId={}", keyVersion, requesterId);
            throw new IllegalArgumentException("keyVersion must be a positive integer");
        }

        log.info("SupportService.decryptPassword() - request by userId={} email={} keyVersion={}",
                requesterId, requesterEmail, keyVersion);

        try {
            // Delegate to AESUtils.decrypt (existing method) — unbox Integer to int
            String plain = aesUtils.decrypt(encryptedPassword, keyVersion.intValue());

            // Audit log (do NOT include plaintext in logs)
            log.info("SupportService.decryptPassword() - decryption successful for keyVersion={} requestedBy={}",
                    keyVersion, requesterId);

            // Return DTO instead of raw String
            return DecryptPasswordResponse.builder()
                    .encryptedPassword(encryptedPassword)
                    .decryptedPassword(plain)
                    .keyVersion(keyVersion)
                    .build();
        } catch (InvalidEncryptionKeyException ex) {
            // predictable condition — log and rethrow for controller to map to 404
            log.warn("SupportService.decryptPassword() - encryption key not found for version {} requestedBy={}: {}",
                    keyVersion, requesterId, ex.getMessage());
            throw ex;
        } catch (IllegalArgumentException ex) {
            log.warn("SupportService.decryptPassword() - invalid request from userId={}: {}",
                    requesterId, ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("SupportService.decryptPassword() - unexpected error for keyVersion={} requestedBy={}: {}",
                    keyVersion, requesterId, ex.getMessage(), ex);
            throw ex;
        }
    }
}

