package com.sid.app.repository;

import com.sid.app.entity.AppSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for AppSubscription entity operations.
 * Provides methods for subscription management.
 *
 * @author Siddhant Patni
 */
@Repository
public interface AppSubscriptionRepository extends JpaRepository<AppSubscription, Long> {

    /**
     * Find subscription by subscription code
     */
    Optional<AppSubscription> findBySubscriptionCode(String subscriptionCode);

    /**
     * Find all active subscriptions
     */
    @Query("SELECT s FROM AppSubscription s WHERE s.isActive = true ORDER BY s.subscriptionName")
    List<AppSubscription> findAllActive();

    /**
     * Find active subscription by code
     */
    @Query("SELECT s FROM AppSubscription s WHERE s.subscriptionCode = :code AND s.isActive = true")
    Optional<AppSubscription> findActiveBySubscriptionCode(@Param("code") String subscriptionCode);

    /**
     * Check if subscription code exists
     */
    boolean existsBySubscriptionCode(String subscriptionCode);

    /**
     * Find subscriptions by name containing search term
     */
    @Query("SELECT s FROM AppSubscription s WHERE s.subscriptionName LIKE %:searchTerm% ORDER BY s.subscriptionName")
    List<AppSubscription> findBySubscriptionNameContainingIgnoreCase(@Param("searchTerm") String searchTerm);
}
