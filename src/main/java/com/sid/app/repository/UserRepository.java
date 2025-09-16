package com.sid.app.repository;

import com.sid.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * @author Siddhant Patni
 */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByName(String name);

    Optional<User> findByEmailOrMobileNumber(String email, String mobileNumber);

    /**
     * Finder for mobile number (used to check uniqueness before updating).
     * Ensure the property name matches your User entity field name (mobileNumber).
     */
    Optional<User> findByMobileNumber(String mobileNumber);

    @Modifying
    @Query(value = "UPDATE users SET password = :password, password_encryption_key_version = :keyVersion, modified_date = now() WHERE user_id = :userId", nativeQuery = true)
    int updatePassword(@Param("userId") Long userId,
                       @Param("password") String password,
                       @Param("keyVersion") Integer keyVersion);

}