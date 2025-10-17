package com.sid.app.repository;

import com.sid.app.entity.TenantUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TenantUserRepository extends JpaRepository<TenantUser, Long> {

    Optional<TenantUser> findByEmail(String email);

    @Query("SELECT tu FROM TenantUser tu WHERE tu.tenantId = :tenantId")
    List<TenantUser> findByTenantId(@Param("tenantId") Long tenantId);

    @Query("SELECT tu FROM TenantUser tu WHERE tu.tenantId = :tenantId AND tu.isActive = true")
    List<TenantUser> findActiveByTenantId(@Param("tenantId") Long tenantId);

    @Query("SELECT tu FROM TenantUser tu WHERE tu.email = :email AND tu.isActive = true")
    Optional<TenantUser> findActiveByEmail(@Param("email") String email);

    @Query("SELECT tu FROM TenantUser tu WHERE tu.tenantId = :tenantId AND tu.roleId = :roleId")
    List<TenantUser> findByTenantIdAndRoleId(@Param("tenantId") Long tenantId, @Param("roleId") Long roleId);

    @Query("SELECT tu FROM TenantUser tu WHERE tu.platformUserId = :platformUserId")
    List<TenantUser> findByPlatformUserId(@Param("platformUserId") Long platformUserId);

    boolean existsByEmail(String email);

    boolean existsByMobileNumber(String mobileNumber);

    Optional<TenantUser> findByTenantUserCode(String tenantUserCode);

    @Query("SELECT tu FROM TenantUser tu WHERE tu.tenantUserCode = :code AND tu.isActive = true")
    Optional<TenantUser> findActiveByTenantUserCode(@Param("code") String tenantUserCode);

    boolean existsByTenantUserCode(String tenantUserCode);

    @Query("SELECT COUNT(tu) FROM TenantUser tu WHERE tu.tenantId = :tenantId")
    Long countByTenantId(@Param("tenantId") Long tenantId);

    @Query("SELECT COUNT(tu) FROM TenantUser tu WHERE tu.tenantId = :tenantId AND tu.isActive = true")
    Long countActiveByTenantId(@Param("tenantId") Long tenantId);

    // New methods for tenant user management
    @Query("SELECT tu FROM TenantUser tu WHERE tu.roleId = :roleId AND tu.isActive = true")
    List<TenantUser> findByRoleIdAndIsActiveTrue(@Param("roleId") Long roleId);

    @Query("SELECT tu FROM TenantUser tu WHERE tu.tenantId = :tenantId AND tu.roleId = :roleId AND tu.isActive = true")
    List<TenantUser> findByTenantIdAndRoleIdAndIsActiveTrue(@Param("tenantId") Long tenantId, @Param("roleId") Long roleId);

    @Query("SELECT tu FROM TenantUser tu WHERE tu.tenantId = :tenantId AND tu.roleId = :roleId AND tu.managerTenantUserId = :managerTenantUserId")
    List<TenantUser> findByTenantIdAndRoleIdAndManagerTenantUserId(@Param("tenantId") Long tenantId, @Param("roleId") Long roleId, @Param("managerTenantUserId") Long managerTenantUserId);

    @Query("SELECT tu FROM TenantUser tu WHERE tu.roleId = :roleId AND (tu.name LIKE %:searchTerm% OR tu.email LIKE %:searchTerm%)")
    List<TenantUser> searchByRoleAndTerm(@Param("roleId") Long roleId, @Param("searchTerm") String searchTerm);

    @Query("SELECT tu FROM TenantUser tu WHERE tu.tenantId = :tenantId AND tu.roleId = :roleId AND (tu.name LIKE %:searchTerm% OR tu.email LIKE %:searchTerm%)")
    List<TenantUser> searchByTenantAndRoleAndTerm(@Param("tenantId") Long tenantId, @Param("roleId") Long roleId, @Param("searchTerm") String searchTerm);

    // Statistics methods for PLATFORM_USER
    @Query("SELECT COUNT(DISTINCT tu.tenantId) FROM TenantUser tu")
    Long countDistinctTenants();

    @Query("SELECT COUNT(tu) FROM TenantUser tu " +
           "JOIN UserRole ur ON tu.roleId = ur.roleId " +
           "WHERE ur.role = :roleCode")
    Long countSuperAdmins(@Param("roleCode") String roleCode);

    @Query("SELECT COUNT(tu) FROM TenantUser tu " +
           "JOIN UserRole ur ON tu.roleId = ur.roleId " +
           "WHERE ur.role = :roleCode")
    Long countAdmins(@Param("roleCode") String roleCode);

    @Query("SELECT COUNT(tu) FROM TenantUser tu " +
           "JOIN UserRole ur ON tu.roleId = ur.roleId " +
           "WHERE ur.role IN :roleCodes")
    Long countUsers(@Param("roleCodes") List<String> roleCodes);

    @Query("SELECT tu.tenantId, COUNT(tu) FROM TenantUser tu " +
           "JOIN UserRole ur ON tu.roleId = ur.roleId " +
           "WHERE ur.role = :roleCode GROUP BY tu.tenantId")
    List<Object[]> countSuperAdminsByTenant(@Param("roleCode") String roleCode);

    @Query("SELECT tu.tenantId, COUNT(tu) FROM TenantUser tu " +
           "JOIN UserRole ur ON tu.roleId = ur.roleId " +
           "WHERE ur.role = :roleCode GROUP BY tu.tenantId")
    List<Object[]> countAdminsByTenant(@Param("roleCode") String roleCode);

    @Query("SELECT tu.tenantId, COUNT(tu) FROM TenantUser tu " +
           "JOIN UserRole ur ON tu.roleId = ur.roleId " +
           "WHERE ur.role IN :roleCodes GROUP BY tu.tenantId")
    List<Object[]> countUsersByTenant(@Param("roleCodes") List<String> roleCodes);

    @Query("SELECT tu.tenantId, COUNT(tu) FROM TenantUser tu GROUP BY tu.tenantId")
    List<Object[]> countTotalUsersByTenant();
}
