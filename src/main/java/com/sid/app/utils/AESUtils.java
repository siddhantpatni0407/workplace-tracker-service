package com.sid.app.utils;

import com.sid.app.config.AESProperties;
import com.sid.app.entity.EncryptionKey;
import com.sid.app.exception.InvalidEncryptionKeyException;
import com.sid.app.service.EncryptionKeyService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * AES Encryption Utility without version prefix in encrypted data.
 * The key version is now stored separately in the users table.
 */
@Slf4j
@Component
public class AESUtils {

    @Autowired
    private EncryptionKeyService keyService;

    @Autowired
    private AESProperties aesProperties;

    private SecretKeySpec latestKeySpec;

    @PostConstruct
    private void init() {
        updateKeySpec();
    }

    /**
     * Updates the latest encryption key specification.
     */
    private synchronized void updateKeySpec() {
        EncryptionKey latestKey = keyService.getLatestKey();
        if (latestKey != null) {
            latestKeySpec = new SecretKeySpec(latestKey.getSecretKey().getBytes(), aesProperties.getAlgorithm());
            log.info("Latest encryption key updated (version: {}).", latestKey.getKeyVersion());
        } else {
            log.error("No encryption key found in the database.");
            throw new InvalidEncryptionKeyException("No encryption key available in the system.", null);
        }
    }

    /**
     * Encrypts the provided data without appending a key version.
     *
     * @param data The plaintext to encrypt.
     * @return The encrypted data.
     */
    public String encrypt(String data) {
        try {
            updateKeySpec(); // Ensure latest key is used
            Cipher cipher = Cipher.getInstance(aesProperties.getAlgorithm());
            cipher.init(Cipher.ENCRYPT_MODE, latestKeySpec);
            byte[] encryptedBytes = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            log.error("Encryption error: {}", e.getMessage());
            throw new InvalidEncryptionKeyException("Encryption failed due to an invalid key configuration.", e);
        }
    }

    /**
     * Decrypts the provided data using the given encryption key version.
     *
     * @param encryptedData The encrypted string.
     * @param keyVersion    The key version used for decryption (stored separately).
     * @return The decrypted plaintext.
     */
    public String decrypt(String encryptedData, int keyVersion) {
        try {
            EncryptionKey key = keyService.getKeyByVersion(keyVersion);
            if (key == null) {
                log.error("No encryption key found for version: {}", keyVersion);
                throw new InvalidEncryptionKeyException("No encryption key found for version: " + keyVersion, null);
            }

            SecretKeySpec keySpec = new SecretKeySpec(key.getSecretKey().getBytes(), aesProperties.getAlgorithm());
            Cipher cipher = Cipher.getInstance(aesProperties.getAlgorithm());
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));

            return new String(decryptedBytes);
        } catch (InvalidEncryptionKeyException ex) {
            throw ex; // Rethrow custom exception for better debugging
        } catch (Exception e) {
            log.error("Decryption error: {}", e.getMessage());
            throw new InvalidEncryptionKeyException("Decryption failed due to an invalid key or corrupted data.", e);
        }
    }

}