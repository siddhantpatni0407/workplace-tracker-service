package com.sid.app.service;

import com.sid.app.constants.AppConstants;
import com.sid.app.entity.Tenant;
import com.sid.app.entity.AppSubscription;
import com.sid.app.model.TenantCreateRequest;
import com.sid.app.model.TenantUpdateRequest;
import com.sid.app.model.TenantDTO;
import com.sid.app.model.TenantStatusUpdateRequest;
import com.sid.app.repository.TenantRepository;
import com.sid.app.repository.AppSubscriptionRepository;
import com.sid.app.repository.TenantUserRepository;
import com.sid.app.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for managing tenants.
 * Handles CRUD operations and business logic for tenant management.
 * Restricted to PLATFORM_USER role.
 *
 * @author Siddhant Patni
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TenantService {

    private final TenantRepository tenantRepository;
    private final AppSubscriptionRepository appSubscriptionRepository;
    private final TenantUserRepository tenantUserRepository;
    private final UserRepository userRepository;
    private final CodeGenerationService codeGenerationService;

    /**
     * Create a new tenant
     */
    @Transactional
    public TenantDTO createTenant(TenantCreateRequest request) {
        log.info("Creating new tenant with name: {}", request.getTenantName());

        // Check if tenant name already exists
        if (tenantRepository.existsByTenantName(request.getTenantName())) {
            throw new IllegalArgumentException("Tenant name already exists: " + request.getTenantName());
        }

        // Generate tenant code based on tenant name
        String tenantCode = codeGenerationService.generateTenantCode(request.getTenantName());

        Tenant tenant = new Tenant();
        tenant.setTenantName(request.getTenantName());
        tenant.setTenantCode(tenantCode);
        tenant.setAppSubscriptionId(request.getSubscriptionId());
        tenant.setContactEmail(request.getContactEmail());
        tenant.setContactPhone(request.getContactPhone());
        tenant.setSubscriptionStartDate(request.getSubscriptionStartDate());
        tenant.setSubscriptionEndDate(request.getSubscriptionEndDate());
        tenant.setIsActive(true);

        Tenant savedTenant = tenantRepository.save(tenant);
        log.info("Tenant created successfully with ID: {} and code: {}", savedTenant.getTenantId(), savedTenant.getTenantCode());

        return convertToDTO(savedTenant);
    }

    /**
     * Get all tenants with pagination
     */
    public Page<TenantDTO> getAllTenants(Pageable pageable) {
        log.info("Fetching all tenants with pagination");
        Page<Tenant> tenants = tenantRepository.findAll(pageable);
        return tenants.map(this::convertToDTO);
    }

    /**
     * Get all active tenants
     */
    public List<TenantDTO> getActiveTenants() {
        log.info("Fetching all active tenants");
        List<Tenant> activeTenants = tenantRepository.findAllActive();
        return activeTenants.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get tenant by ID
     */
    public TenantDTO getTenantById(Long tenantId) {
        log.info("Fetching tenant by ID: {}", tenantId);
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Tenant not found with ID: " + tenantId));
        return convertToDTO(tenant);
    }

    /**
     * Get tenant by tenant code
     */
    public TenantDTO getTenantByCode(String tenantCode) {
        log.info("Fetching tenant by code: {}", tenantCode);
        Tenant tenant = tenantRepository.findByTenantCode(tenantCode)
                .orElseThrow(() -> new EntityNotFoundException("Tenant not found with code: " + tenantCode));
        return convertToDTO(tenant);
    }

    /**
     * Update tenant
     */
    @Transactional
    public TenantDTO updateTenant(Long tenantId, TenantUpdateRequest request) {
        log.info("Updating tenant with ID: {}", tenantId);

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Tenant not found with ID: " + tenantId));

        // Update only provided fields
        if (request.getTenantName() != null && !request.getTenantName().trim().isEmpty()) {
            // Check if new name conflicts with existing tenant
            if (!request.getTenantName().equals(tenant.getTenantName()) &&
                    tenantRepository.existsByTenantName(request.getTenantName())) {
                throw new IllegalArgumentException("Tenant name already exists: " + request.getTenantName());
            }
            tenant.setTenantName(request.getTenantName());
        }

        tenant.setAppSubscriptionId(request.getSubscriptionId());

        if (request.getContactEmail() != null) {
            tenant.setContactEmail(request.getContactEmail());
        }

        if (request.getContactPhone() != null) {
            tenant.setContactPhone(request.getContactPhone());
        }

        if (request.getSubscriptionStartDate() != null) {
            tenant.setSubscriptionStartDate(request.getSubscriptionStartDate());
        }

        if (request.getSubscriptionEndDate() != null) {
            tenant.setSubscriptionEndDate(request.getSubscriptionEndDate());
        }

        Tenant updatedTenant = tenantRepository.save(tenant);
        log.info("Tenant updated successfully with ID: {}", updatedTenant.getTenantId());

        return convertToDTO(updatedTenant);
    }

    /**
     * Update tenant status (activate/deactivate)
     */
    @Transactional
    public TenantDTO updateTenantStatus(TenantStatusUpdateRequest request) {
        log.info("Updating status for tenant ID: {} to {}", request.getTenantId(), request.getIsActive());

        Tenant tenant = tenantRepository.findById(request.getTenantId())
                .orElseThrow(() -> new EntityNotFoundException("Tenant not found with ID: " + request.getTenantId()));

        tenant.setIsActive(request.getIsActive());
        Tenant updatedTenant = tenantRepository.save(tenant);

        log.info("Tenant status updated successfully for ID: {}, new status: {}",
                updatedTenant.getTenantId(), updatedTenant.getIsActive());

        return convertToDTO(updatedTenant);
    }

    /**
     * Delete tenant (soft delete by deactivating)
     */
    @Transactional
    public void deleteTenant(Long tenantId) {
        log.info("Deleting (deactivating) tenant with ID: {}", tenantId);

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Tenant not found with ID: " + tenantId));

        tenant.setIsActive(false);
        tenantRepository.save(tenant);

        log.info("Tenant deactivated successfully with ID: {}", tenantId);
    }

    /**
     * Search tenants by name
     */
    public List<TenantDTO> searchTenantsByName(String searchTerm) {
        log.info("Searching tenants by name containing: {}", searchTerm);

        List<Tenant> tenants = tenantRepository.findByTenantNameContainingIgnoreCase(searchTerm);
        return tenants.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Update tenant's subscription plan
     */
    @Transactional
    public TenantDTO updateTenantSubscription(String tenantCode, String newSubscriptionCode) {
        log.info("Updating tenant subscription - tenant: {}, new subscription: {}", tenantCode, newSubscriptionCode);

        // Find the tenant by tenant code
        Tenant tenant = tenantRepository.findByTenantCode(tenantCode)
                .orElseThrow(() -> new EntityNotFoundException("Tenant not found with code: " + tenantCode));

        // Find the new subscription by subscription code
        AppSubscription newSubscription = appSubscriptionRepository.findBySubscriptionCode(newSubscriptionCode)
                .orElseThrow(() -> new EntityNotFoundException("Subscription not found with code: " + newSubscriptionCode));

        // Validate that the subscription is active
        if (!newSubscription.getIsActive()) {
            throw new IllegalArgumentException("Cannot assign inactive subscription: " + newSubscriptionCode);
        }

        Long oldSubscriptionId = tenant.getAppSubscriptionId();

        // Update tenant's subscription
        tenant.setAppSubscriptionId(newSubscription.getAppSubscriptionId());

        // Save updated tenant
        Tenant updatedTenant = tenantRepository.save(tenant);
        log.info("Successfully updated tenant {} subscription from {} to {}",
                tenantCode, oldSubscriptionId, newSubscription.getAppSubscriptionId());

        // Create DTO with fresh subscription data to avoid lazy loading issues
        TenantDTO tenantDTO = convertToDTO(updatedTenant);

        // Ensure we set the correct subscription details from the new subscription
        tenantDTO.setSubscriptionCode(newSubscription.getSubscriptionCode());
        tenantDTO.setSubscriptionName(newSubscription.getSubscriptionName());
        tenantDTO.setAppSubscriptionId(newSubscription.getAppSubscriptionId());

        return tenantDTO;
    }

    /**
     * Get tenant statistics
     */
    public TenantDTO getTenantStats(Long tenantId) {
        log.info("Fetching statistics for tenant ID: {}", tenantId);

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Tenant not found with ID: " + tenantId));

        TenantDTO tenantDTO = convertToDTO(tenant);

        // Get user counts for this tenant
        Long totalUsers = tenantUserRepository.countByTenantId(tenantId) +
                userRepository.countByTenantUserId(tenantId);

        Long activeUsers = tenantUserRepository.countActiveByTenantId(tenantId) +
                userRepository.countActiveByTenantUserId(tenantId);

        tenantDTO.setTotalUsers(totalUsers);
        tenantDTO.setActiveUsers(activeUsers);

        return tenantDTO;
    }

    /**
     * Get users for a specific tenant
     */
    public List<Object> getTenantUsers(Long tenantId) {
        log.info("Fetching users for tenant ID: {}", tenantId);

        // Verify tenant exists
        if (!tenantRepository.existsById(tenantId)) {
            throw new EntityNotFoundException("Tenant not found with ID: " + tenantId);
        }

        // Get tenant users (ADMIN, SUPER_ADMIN)
        List<Object> tenantUsers = tenantUserRepository.findActiveByTenantId(tenantId)
                .stream()
                .map(tenantUser -> (Object) tenantUser)
                .collect(Collectors.toList());

        // Get regular users (USER, MANAGER) - need to find them via tenant_user relationship
        List<Object> regularUsers = userRepository.findActiveByTenantUserId(tenantId)
                .stream()
                .map(user -> (Object) user)
                .collect(Collectors.toList());

        tenantUsers.addAll(regularUsers);
        return tenantUsers;
    }

    /**
     * Convert Tenant entity to TenantDTO
     */
    private TenantDTO convertToDTO(Tenant tenant) {
        TenantDTO dto = new TenantDTO();
        dto.setTenantId(tenant.getTenantId());
        dto.setTenantName(tenant.getTenantName());
        dto.setTenantCode(tenant.getTenantCode());
        dto.setAppSubscriptionId(tenant.getAppSubscriptionId());
        dto.setContactEmail(tenant.getContactEmail());
        dto.setContactPhone(tenant.getContactPhone());
        dto.setIsActive(tenant.getIsActive());
        dto.setSubscriptionStartDate(tenant.getSubscriptionStartDate());
        dto.setSubscriptionEndDate(tenant.getSubscriptionEndDate());
        dto.setCreatedDate(tenant.getCreatedDate());
        dto.setModifiedDate(tenant.getModifiedDate());

        // Get subscription details (both code and name)
        if (tenant.getAppSubscription() != null) {
            dto.setSubscriptionCode(tenant.getAppSubscription().getSubscriptionCode());
            dto.setSubscriptionName(tenant.getAppSubscription().getSubscriptionName());
        } else if (tenant.getAppSubscriptionId() != null) {
            // Fallback: fetch subscription details if lazy loading didn't work
            appSubscriptionRepository.findById(tenant.getAppSubscriptionId())
                    .ifPresent(subscription -> {
                        dto.setSubscriptionCode(subscription.getSubscriptionCode());
                        dto.setSubscriptionName(subscription.getSubscriptionName());
                    });
        }

        return dto;
    }

    /**
     * Resolve subscription code to ID
     */
    private Long resolveSubscriptionCodeToId(String subscriptionCode) {
        if (subscriptionCode == null || subscriptionCode.trim().isEmpty()) {
            return null;
        }

        // Find subscription by code
        AppSubscription subscription = appSubscriptionRepository.findBySubscriptionCode(subscriptionCode)
                .orElseThrow(() -> new IllegalArgumentException("Invalid subscription code: " + subscriptionCode));

        return subscription.getAppSubscriptionId();
    }
}
