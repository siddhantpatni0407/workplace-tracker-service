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
}
