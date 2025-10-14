package com.sid.app.repository;

import com.sid.app.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {
    
    Optional<Tenant> findByTenantName(String tenantName);
    
    @Query("SELECT t FROM Tenant t WHERE t.isActive = true")
    List<Tenant> findAllActive();
    
    @Query("SELECT t FROM Tenant t WHERE t.tenantName = :name AND t.isActive = true")
    Optional<Tenant> findActiveByTenantName(@Param("name") String tenantName);
    
    @Query("SELECT t FROM Tenant t WHERE t.appSubscriptionId = :subscriptionId AND t.isActive = true")
    List<Tenant> findActiveBySubscriptionId(@Param("subscriptionId") Long subscriptionId);
    
    boolean existsByTenantName(String tenantName);

    Optional<Tenant> findByTenantCode(String tenantCode);

    @Query("SELECT t FROM Tenant t WHERE t.tenantCode = :code AND t.isActive = true")
    Optional<Tenant> findActiveByTenantCode(@Param("code") String tenantCode);

    boolean existsByTenantCode(String tenantCode);
}
