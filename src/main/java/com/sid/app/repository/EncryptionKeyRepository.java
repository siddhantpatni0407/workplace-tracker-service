package com.sid.app.repository;

import com.sid.app.entity.EncryptionKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Siddhant Patni
 */
@Repository
public interface EncryptionKeyRepository extends JpaRepository<EncryptionKey, Long> {

    EncryptionKey findTopByOrderByKeyVersionDesc();

    EncryptionKey findByKeyVersion(int keyVersion);

}