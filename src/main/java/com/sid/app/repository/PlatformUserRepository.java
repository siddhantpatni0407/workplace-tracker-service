package com.sid.app.repository;

import com.sid.app.entity.PlatformUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for PlatformUser entity operations.
 * Provides methods for platform user authentication and management.
 *
 * @author Siddhant Patni
 */
@Repository
public interface PlatformUserRepository extends JpaRepository<PlatformUser, Long> {

    /**
     * Find platform user by email address.
     * Used for login and uniqueness validation.
     *
     * @param email the email address
     * @return Optional containing the platform user if found
     */
    Optional<PlatformUser> findByEmail(String email);

    /**
     * Find platform user by mobile number.
     * Used for login via mobile and uniqueness validation.
     *
     * @param mobileNumber the mobile number
     * @return Optional containing the platform user if found
     */
    Optional<PlatformUser> findByMobileNumber(String mobileNumber);

    /**
     * Find platform user by email or mobile number.
     * Used for login functionality where user can use either email or mobile.
     *
     * @param email the email address
     * @param mobileNumber the mobile number
     * @return Optional containing the platform user if found
     */
    Optional<PlatformUser> findByEmailOrMobileNumber(String email, String mobileNumber);

    /**
     * Find platform user by platform user code.
     * Used for SUPER_ADMIN registration validation.
     *
     * @param platformUserCode the platform user code
     * @return Optional containing the platform user if found
     */
    Optional<PlatformUser> findByPlatformUserCode(String platformUserCode);

    /**
     * Find active platform user by platform user code.
     * Used for SUPER_ADMIN registration validation.
     *
     * @param platformUserCode the platform user code
     * @return Optional containing the active platform user if found
     */
    @Query("SELECT pu FROM PlatformUser pu WHERE pu.platformUserCode = :code AND pu.isActive = true")
    Optional<PlatformUser> findActiveByPlatformUserCode(@Param("code") String platformUserCode);

    /**
     * Check if email exists in the system.
     * Used for registration validation.
     *
     * @param email the email address
     * @return true if email exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Check if mobile number exists in the system.
     * Used for registration validation.
     *
     * @param mobileNumber the mobile number
     * @return true if mobile number exists, false otherwise
     */
    boolean existsByMobileNumber(String mobileNumber);

    /**
     * Check if platform user code exists in the system.
     * Used for registration validation.
     *
     * @param platformUserCode the platform user code
     * @return true if code exists, false otherwise
     */
    boolean existsByPlatformUserCode(String platformUserCode);

    /**
     * Update password for a platform user.
     * Used for password change functionality.
     *
     * @param platformUserId the platform user ID
     * @param encryptedPassword the new encrypted password
     * @param keyVersion the encryption key version
     * @return number of rows updated
     */
    @Modifying
    @Query("UPDATE PlatformUser pu SET pu.password = :password, pu.passwordEncryptionKeyVersion = :keyVersion WHERE pu.platformUserId = :id")
    int updatePassword(@Param("id") Long platformUserId, @Param("password") String encryptedPassword, @Param("keyVersion") Integer keyVersion);

    /**
     * Find platform user by email and active status.
     * Used for login validation.
     *
     * @param email the email address
     * @return Optional containing the active platform user if found
     */
    @Query("SELECT pu FROM PlatformUser pu WHERE pu.email = :email AND pu.isActive = true")
    Optional<PlatformUser> findActiveByEmail(@Param("email") String email);
}
