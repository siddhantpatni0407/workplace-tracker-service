package com.sid.app.repository;

import com.sid.app.entity.AppSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppSubscriptionRepository extends JpaRepository<AppSubscription, Long> {

    Optional<AppSubscription> findBySubscriptionCode(String subscriptionCode);

    @Query("SELECT aps FROM AppSubscription aps WHERE aps.isActive = true")
    List<AppSubscription> findAllActive();

    @Query("SELECT aps FROM AppSubscription aps WHERE aps.subscriptionCode = :code AND aps.isActive = true")
    Optional<AppSubscription> findActiveBySubscriptionCode(@Param("code") String subscriptionCode);

    boolean existsBySubscriptionCode(String subscriptionCode);
}
