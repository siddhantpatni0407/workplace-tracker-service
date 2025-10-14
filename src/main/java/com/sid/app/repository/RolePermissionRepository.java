package com.sid.app.repository;

import com.sid.app.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {

    Optional<RolePermission> findByRoleId(Long roleId);

    @Query("SELECT rp FROM RolePermission rp WHERE rp.roleId = :roleId")
    Optional<RolePermission> findPermissionsByRoleId(@Param("roleId") Long roleId);

    boolean existsByRoleId(Long roleId);
}
