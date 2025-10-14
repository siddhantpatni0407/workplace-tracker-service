package com.sid.app.repository;

import com.sid.app.entity.UserRoleMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRoleMappingRepository extends JpaRepository<UserRoleMapping, Long> {

    @Query("SELECT urm FROM UserRoleMapping urm WHERE urm.userId = :userId")
    List<UserRoleMapping> findByUserId(@Param("userId") Long userId);

    @Query("SELECT urm FROM UserRoleMapping urm WHERE urm.roleId = :roleId")
    List<UserRoleMapping> findByRoleId(@Param("roleId") Long roleId);

    @Query("SELECT urm FROM UserRoleMapping urm WHERE urm.userId = :userId AND urm.roleId = :roleId")
    Optional<UserRoleMapping> findByUserIdAndRoleId(@Param("userId") Long userId, @Param("roleId") Long roleId);

    boolean existsByUserIdAndRoleId(Long userId, Long roleId);

    void deleteByUserIdAndRoleId(Long userId, Long roleId);
}
