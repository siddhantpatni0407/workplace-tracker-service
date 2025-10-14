package com.sid.app.repository;

import com.sid.app.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    Optional<UserRole> findByRole(String role);

    @Query("SELECT ur FROM UserRole ur WHERE UPPER(ur.role) = UPPER(:role)")
    Optional<UserRole> findByRoleIgnoreCase(@Param("role") String role);

    boolean existsByRole(String role);
}
