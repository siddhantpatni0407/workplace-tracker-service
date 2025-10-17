package com.sid.app.repository;

import com.sid.app.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
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

    /**
     * Find users by tenant user ID
     */
    @Query("SELECT u FROM User u WHERE u.tenantUserId = :tenantUserId")
    List<User> findByTenantUserId(@Param("tenantUserId") Long tenantUserId);

    /**
     * Find users by multiple tenant user IDs
     */
    @Query("SELECT u FROM User u WHERE u.tenantUserId IN :tenantUserIds")
    List<User> findByTenantUserIdIn(@Param("tenantUserIds") List<Long> tenantUserIds);

    /**
     * Find active users by tenant user ID
     */
    @Query("SELECT u FROM User u WHERE u.tenantUserId = :tenantUserId AND u.isActive = true")
    List<User> findActiveByTenantUserId(@Param("tenantUserId") Long tenantUserId);

    /**
     * Count users by tenant user ID
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.tenantUserId = :tenantUserId")
    Long countByTenantUserId(@Param("tenantUserId") Long tenantUserId);

    /**
     * Count active users by tenant user ID
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.tenantUserId = :tenantUserId AND u.isActive = true")
    Long countActiveByTenantUserId(@Param("tenantUserId") Long tenantUserId);

    @Modifying
    @Query(value = "UPDATE users SET password = :password, password_encryption_key_version = :keyVersion, modified_date = now() WHERE user_id = :userId", nativeQuery = true)
    int updatePassword(@Param("userId") Long userId,
                       @Param("password") String password,
                       @Param("keyVersion") Integer keyVersion);

    /**
     * Find active users with their profiles for Special Days functionality
     */
    @Query("SELECT u FROM User u " +
           "JOIN UserRole ur ON u.roleId = ur.roleId " +
           "WHERE u.isActive = true " +
           "AND ur.role = 'USER' " +
           "ORDER BY u.name")
    List<User> findActiveUsersWithProfiles();

    /**
     * Find active users with their profiles and addresses with pagination
     */
    @Query("SELECT u FROM User u " +
           "JOIN UserRole ur ON u.roleId = ur.roleId " +
           "WHERE u.isActive = true " +
           "AND ur.role = 'USER' " +
           "ORDER BY u.createdDate DESC")
    Page<User> findActiveUsersWithProfiles(Pageable pageable);

    /**
     * Find users by role
     */
    @Query("SELECT u FROM User u WHERE u.roleId = :roleId")
    List<User> findByRoleId(@Param("roleId") Long roleId);

    /**
     * Find active users by role
     */
    @Query("SELECT u FROM User u WHERE u.roleId = :roleId AND u.isActive = true")
    List<User> findActiveByRoleId(@Param("roleId") Long roleId);

    /**
     * Check if email exists for different user
     */
    boolean existsByEmailAndUserIdNot(String email, Long userId);

    /**
     * Check if mobile exists for different user
     */
    boolean existsByMobileNumberAndUserIdNot(String mobileNumber, Long userId);

    /**
     * Find users by tenant through tenant user relationship
     */
    @Query("SELECT u FROM User u " +
           "JOIN TenantUser tu ON u.tenantUserId = tu.tenantUserId " +
           "WHERE tu.tenantId = :tenantId")
    List<User> findByTenantId(@Param("tenantId") Long tenantId);

    /**
     * Find active users by tenant
     */
    @Query("SELECT u FROM User u " +
           "JOIN TenantUser tu ON u.tenantUserId = tu.tenantUserId " +
           "WHERE tu.tenantId = :tenantId AND u.isActive = true")
    List<User> findActiveByTenantId(@Param("tenantId") Long tenantId);

    /**
     * Count users by tenant
     */
    @Query("SELECT COUNT(u) FROM User u " +
           "JOIN TenantUser tu ON u.tenantUserId = tu.tenantUserId " +
           "WHERE tu.tenantId = :tenantId")
    Long countByTenantId(@Param("tenantId") Long tenantId);

    /**
     * Count active users by tenant
     */
    @Query("SELECT COUNT(u) FROM User u " +
           "JOIN TenantUser tu ON u.tenantUserId = tu.tenantUserId " +
           "WHERE tu.tenantId = :tenantId AND u.isActive = true")
    Long countActiveByTenantId(@Param("tenantId") Long tenantId);

    /**
     * Search users by name or email within tenant
     */
    @Query("SELECT u FROM User u " +
           "JOIN TenantUser tu ON u.tenantUserId = tu.tenantUserId " +
           "WHERE tu.tenantId = :tenantId AND u.isActive = true " +
           "AND (LOWER(u.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<User> searchUsersByTenant(@Param("tenantId") Long tenantId, @Param("searchTerm") String searchTerm);
}