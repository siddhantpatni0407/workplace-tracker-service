package com.sid.app.service;

import com.sid.app.entity.AppSubscription;
import com.sid.app.model.SubscriptionDTO;
import com.sid.app.repository.AppSubscriptionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for managing subscriptions.
 * Handles read operations for subscription management.
 * Restricted to PLATFORM_USER role.
 *
 * @author Siddhant Patni
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {

    private final AppSubscriptionRepository appSubscriptionRepository;

    /**
     * Get all subscriptions
     */
    public List<SubscriptionDTO> getAllSubscriptions() {
        log.info("Fetching all subscriptions");
        List<AppSubscription> subscriptions = appSubscriptionRepository.findAll();
        return subscriptions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all active subscriptions
     */
    public List<SubscriptionDTO> getActiveSubscriptions() {
        log.info("Fetching all active subscriptions");
        List<AppSubscription> activeSubscriptions = appSubscriptionRepository.findAllActive();
        return activeSubscriptions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get subscription by code
     */
    public SubscriptionDTO getSubscriptionByCode(String subscriptionCode) {
        log.info("Fetching subscription by code: {}", subscriptionCode);
        Optional<AppSubscription> subscription = appSubscriptionRepository.findBySubscriptionCode(subscriptionCode);

        if (subscription.isEmpty()) {
            throw new EntityNotFoundException("Subscription not found with code: " + subscriptionCode);
        }

        return convertToDTO(subscription.get());
    }

    /**
     * Convert AppSubscription entity to SubscriptionDTO
     */
    private SubscriptionDTO convertToDTO(AppSubscription subscription) {
        SubscriptionDTO dto = new SubscriptionDTO();
        dto.setAppSubscriptionId(subscription.getAppSubscriptionId());
        dto.setSubscriptionCode(subscription.getSubscriptionCode());
        dto.setSubscriptionName(subscription.getSubscriptionName());
        dto.setDescription(subscription.getDescription());
        dto.setIsActive(subscription.getIsActive());
        dto.setCreatedDate(subscription.getCreatedDate());
        dto.setModifiedDate(subscription.getModifiedDate());
        return dto;
    }
}
