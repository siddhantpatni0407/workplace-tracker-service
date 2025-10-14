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
     * Check if email exists in the system.
     * Used for registration validation.
     *
     * @param email the email address to check
     * @return true if email exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Check if mobile number exists in the system.
     * Used for registration validation.
     *
     * @param mobileNumber the mobile number to check
     * @return true if mobile number exists, false otherwise
     */
    boolean existsByMobileNumber(String mobileNumber);

    /**
     * Find platform user by platform user code.
     * Used for retrieving user details using the unique code.
     *
     * @param platformUserCode the platform user code
     * @return Optional containing the platform user if found
     */
    Optional<PlatformUser> findByPlatformUserCode(String platformUserCode);

    /**
     * Find active platform user by platform user code.
     * Used for retrieving active user details using the unique code.
     *
     * @param code the platform user code
     * @return Optional containing the active platform user if found
     */
    @Query("SELECT pu FROM PlatformUser pu WHERE pu.platformUserCode = :code AND pu.isActive = true")
    Optional<PlatformUser> findActiveByPlatformUserCode(@Param("code") String platformUserCode);

    /**
     * Check if platform user code exists in the system.
     * Used for registration validation.
     *
     * @param platformUserCode the platform user code to check
     * @return true if platform user code exists, false otherwise
     */
    boolean existsByPlatformUserCode(String platformUserCode);

    /**
     * Find active platform users.
     * Used to get only non-deactivated platform users.
     *
     * @param isActive the active status
     * @return Optional containing the platform user if found and active
     */
    Optional<PlatformUser> findByPlatformUserIdAndIsActive(Long platformUserId, Boolean isActive);

    /**
     * Update login attempts for a platform user.
     * Used for account lockout functionality.
     *
     * @param platformUserId the platform user ID
     * @param loginAttempts the number of login attempts
     * @return number of rows affected
     */
    @Modifying
    @Query("UPDATE PlatformUser p SET p.loginAttempts = :loginAttempts, p.modifiedDate = CURRENT_TIMESTAMP WHERE p.platformUserId = :platformUserId")
    int updateLoginAttempts(@Param("platformUserId") Long platformUserId, @Param("loginAttempts") Integer loginAttempts);

    /**
     * Update account locked status for a platform user.
     * Used for account lockout and unlock functionality.
     *
     * @param platformUserId the platform user ID
     * @param accountLocked the account locked status
     * @return number of rows affected
     */
    @Modifying
    @Query("UPDATE PlatformUser p SET p.accountLocked = :accountLocked, p.modifiedDate = CURRENT_TIMESTAMP WHERE p.platformUserId = :platformUserId")
    int updateAccountLockedStatus(@Param("platformUserId") Long platformUserId, @Param("accountLocked") Boolean accountLocked);

    /**
     * Update password for a platform user.
     * Used for password reset and change functionality.
     *
     * @param platformUserId the platform user ID
     * @param password the new encrypted password
     * @param keyVersion the encryption key version
     * @return number of rows affected
     */
    @Modifying
    @Query("UPDATE PlatformUser p SET p.password = :password, p.passwordEncryptionKeyVersion = :keyVersion, p.modifiedDate = CURRENT_TIMESTAMP WHERE p.platformUserId = :platformUserId")
    int updatePassword(@Param("platformUserId") Long platformUserId,
                       @Param("password") String password,
                       @Param("keyVersion") Integer keyVersion);

    /**
     * Reset login attempts to 0 after successful login.
     * Used after successful authentication.
     *
     * @param platformUserId the platform user ID
     * @return number of rows affected
     */
    @Modifying
    @Query("UPDATE PlatformUser p SET p.loginAttempts = 0, p.modifiedDate = CURRENT_TIMESTAMP WHERE p.platformUserId = :platformUserId")
    int resetLoginAttempts(@Param("platformUserId") Long platformUserId);
}
