package com.sid.app.service;

import com.sid.app.config.AESProperties;
import com.sid.app.entity.EncryptionKey;
import com.sid.app.exception.InvalidEncryptionKeyException;
import com.sid.app.repository.EncryptionKeyRepository;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class EncryptionKeyService {

    @Autowired
    private EncryptionKeyRepository repository;

    @Autowired
    private AESProperties aesProperties;

    @Getter
    private volatile EncryptionKey latestKey;

    @PostConstruct
    public void init() {
        updateLatestKey();
    }

    /**
     * Ensures that the latest encryption key is properly managed.
     */
    private synchronized void updateLatestKey() {
        latestKey = repository.findTopByOrderByKeyVersionDesc();

        if (latestKey == null || !latestKey.getSecretKey().equals(aesProperties.getSecretKey())) {
            log.info("Creating new encryption key. Previous key version: {}", latestKey == null ? "None" : latestKey.getKeyVersion());

            EncryptionKey newKey = new EncryptionKey();
            newKey.setKeyVersion(latestKey == null ? 1 : latestKey.getKeyVersion() + 1);
            newKey.setSecretKey(aesProperties.getSecretKey());

            latestKey = repository.save(newKey);
            log.info("New encryption key stored with version: {}", latestKey.getKeyVersion());
        }
    }

    /**
     * Retrieves an encryption key by version.
     *
     * @param keyVersion The version of the encryption key.
     * @return The corresponding EncryptionKey entity.
     * @throws InvalidEncryptionKeyException if the key is not found.
     */
    public EncryptionKey getKeyByVersion(int keyVersion) {
        Optional<EncryptionKey> encryptionKey = Optional.ofNullable(repository.findByKeyVersion(keyVersion));
        return encryptionKey.orElseThrow(() -> {
            log.error("No encryption key found for version: {}", keyVersion);
            return new InvalidEncryptionKeyException("Encryption key not found for version: " + keyVersion, null);
        });
    }

}