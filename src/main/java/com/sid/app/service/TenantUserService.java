package com.sid.app.service;

import com.sid.app.entity.TenantUser;
import com.sid.app.entity.UserRole;
import com.sid.app.model.TenantUserDTO;
import com.sid.app.model.UserStatusUpdateRequest;
import com.sid.app.repository.TenantUserRepository;
import com.sid.app.repository.UserRoleRepository;
import com.sid.app.repository.TenantRepository;
import com.sid.app.repository.PlatformUserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing tenant users (SUPER_ADMIN, ADMIN)
 * Provides operations for Platform User to manage Super Admins and Super Admin to manage Admins
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TenantUserService {

    private final TenantUserRepository tenantUserRepository;
    private final UserRoleRepository userRoleRepository;
    private final TenantRepository tenantRepository;
    private final PlatformUserRepository platformUserRepository;

    // ==================== PLATFORM USER APIs - Super Admin Management ====================

    /**
     * Get all Super Admins (Platform User access)
     */
    public List<TenantUserDTO> getAllSuperAdmins() {
        log.info("Fetching all Super Admins from the database");

        Optional<UserRole> superAdminRole = userRoleRepository.findByRoleIgnoreCase("SUPER_ADMIN");
        if (superAdminRole.isEmpty()) {
            log.warn("SUPER_ADMIN role not found in database");
            return List.of();
        }

        List<TenantUser> superAdmins = tenantUserRepository.findByRoleIdAndIsActiveTrue(superAdminRole.get().getRoleId());
        return superAdmins.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get Super Admins by tenant ID
     */
    public List<TenantUserDTO> getSuperAdminsByTenantId(Long tenantId) {
        log.info("Fetching Super Admins for tenant ID: {}", tenantId);

        Optional<UserRole> superAdminRole = userRoleRepository.findByRoleIgnoreCase("SUPER_ADMIN");
        if (superAdminRole.isEmpty()) {
            return List.of();
        }

        List<TenantUser> superAdmins = tenantUserRepository.findByTenantIdAndRoleIdAndIsActiveTrue(
                tenantId, superAdminRole.get().getRoleId());
        return superAdmins.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Search Super Admins by name or email
     */
    public List<TenantUserDTO> searchSuperAdmins(String searchTerm) {
        log.info("Searching Super Admins with term: {}", searchTerm);

        Optional<UserRole> superAdminRole = userRoleRepository.findByRoleIgnoreCase("SUPER_ADMIN");
        if (superAdminRole.isEmpty()) {
            return List.of();
        }

        List<TenantUser> superAdmins = tenantUserRepository.searchByRoleAndTerm(
                superAdminRole.get().getRoleId(), searchTerm);
        return superAdmins.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get Super Admin by ID
     */
    public TenantUserDTO getSuperAdminById(Long tenantUserId) {
        log.info("Fetching Super Admin with ID: {}", tenantUserId);

        TenantUser tenantUser = tenantUserRepository.findById(tenantUserId)
                .orElseThrow(() -> new EntityNotFoundException("Super Admin not found with ID: " + tenantUserId));

        // Verify it's actually a Super Admin
        Optional<UserRole> role = userRoleRepository.findById(tenantUser.getRoleId());
        if (role.isEmpty() || !"SUPER_ADMIN".equalsIgnoreCase(role.get().getRole())) {
            throw new EntityNotFoundException("User is not a Super Admin");
        }

        return convertToDTO(tenantUser);
    }

    /**
     * Update Super Admin status (Platform User access)
     */
    @Transactional
    public TenantUserDTO updateSuperAdminStatus(Long tenantUserId, UserStatusUpdateRequest statusRequest) {
        log.info("Updating Super Admin status for ID: {}", tenantUserId);

        TenantUser tenantUser = tenantUserRepository.findById(tenantUserId)
                .orElseThrow(() -> new EntityNotFoundException("Super Admin not found with ID: " + tenantUserId));

        // Verify it's actually a Super Admin
        Optional<UserRole> role = userRoleRepository.findById(tenantUser.getRoleId());
        if (role.isEmpty() || !"SUPER_ADMIN".equalsIgnoreCase(role.get().getRole())) {
            throw new EntityNotFoundException("User is not a Super Admin");
        }

        tenantUser.setIsActive(statusRequest.getIsActive());
        if (Boolean.FALSE.equals(statusRequest.getIsActive())) {
            tenantUser.setAccountLocked(true);
        }

        TenantUser updated = tenantUserRepository.save(tenantUser);
        return convertToDTO(updated);
    }

    // ==================== SUPER ADMIN APIs - Admin Management ====================

    /**
     * Get all Admins under a specific Super Admin
     */
    public List<TenantUserDTO> getAdminsBySuperAdmin(Long superAdminId) {
        log.info("Fetching Admins under Super Admin ID: {}", superAdminId);

        // Verify the Super Admin exists
        TenantUser superAdmin = tenantUserRepository.findById(superAdminId)
                .orElseThrow(() -> new EntityNotFoundException("Super Admin not found with ID: " + superAdminId));

        Optional<UserRole> adminRole = userRoleRepository.findByRoleIgnoreCase("ADMIN");
        if (adminRole.isEmpty()) {
            return List.of();
        }

        // Get Admins managed by this Super Admin (same tenant)
        List<TenantUser> admins = tenantUserRepository.findByTenantIdAndRoleIdAndManagerTenantUserId(
                superAdmin.getTenantId(), adminRole.get().getRoleId(), superAdminId);
        return admins.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all Admins in the same tenant as Super Admin
     */
    public List<TenantUserDTO> getAdminsByTenant(Long superAdminId) {
        log.info("Fetching all Admins in tenant for Super Admin ID: {}", superAdminId);

        TenantUser superAdmin = tenantUserRepository.findById(superAdminId)
                .orElseThrow(() -> new EntityNotFoundException("Super Admin not found with ID: " + superAdminId));

        Optional<UserRole> adminRole = userRoleRepository.findByRoleIgnoreCase("ADMIN");
        if (adminRole.isEmpty()) {
            return List.of();
        }

        List<TenantUser> admins = tenantUserRepository.findByTenantIdAndRoleIdAndIsActiveTrue(
                superAdmin.getTenantId(), adminRole.get().getRoleId());
        return admins.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Search Admins in Super Admin's tenant
     */
    public List<TenantUserDTO> searchAdminsByTenant(Long superAdminId, String searchTerm) {
        log.info("Searching Admins in Super Admin's tenant with term: {}", searchTerm);

        TenantUser superAdmin = tenantUserRepository.findById(superAdminId)
                .orElseThrow(() -> new EntityNotFoundException("Super Admin not found with ID: " + superAdminId));

        Optional<UserRole> adminRole = userRoleRepository.findByRoleIgnoreCase("ADMIN");
        if (adminRole.isEmpty()) {
            return List.of();
        }

        List<TenantUser> admins = tenantUserRepository.searchByTenantAndRoleAndTerm(
                superAdmin.getTenantId(), adminRole.get().getRoleId(), searchTerm);
        return admins.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get Admin by ID (Super Admin access - must be in same tenant)
     */
    public TenantUserDTO getAdminById(Long superAdminId, Long adminId) {
        log.info("Fetching Admin with ID: {} for Super Admin: {}", adminId, superAdminId);

        TenantUser superAdmin = tenantUserRepository.findById(superAdminId)
                .orElseThrow(() -> new EntityNotFoundException("Super Admin not found with ID: " + superAdminId));

        TenantUser admin = tenantUserRepository.findById(adminId)
                .orElseThrow(() -> new EntityNotFoundException("Admin not found with ID: " + adminId));

        // Verify Admin is in same tenant
        if (!admin.getTenantId().equals(superAdmin.getTenantId())) {
            throw new EntityNotFoundException("Admin not found in your tenant");
        }

        // Verify it's actually an Admin
        Optional<UserRole> role = userRoleRepository.findById(admin.getRoleId());
        if (role.isEmpty() || !"ADMIN".equalsIgnoreCase(role.get().getRole())) {
            throw new EntityNotFoundException("User is not an Admin");
        }

        return convertToDTO(admin);
    }

    /**
     * Update Admin status (Super Admin access)
     */
    @Transactional
    public TenantUserDTO updateAdminStatus(Long superAdminId, Long adminId, UserStatusUpdateRequest statusRequest) {
        log.info("Updating Admin status for ID: {} by Super Admin: {}", adminId, superAdminId);

        TenantUser superAdmin = tenantUserRepository.findById(superAdminId)
                .orElseThrow(() -> new EntityNotFoundException("Super Admin not found with ID: " + superAdminId));

        TenantUser admin = tenantUserRepository.findById(adminId)
                .orElseThrow(() -> new EntityNotFoundException("Admin not found with ID: " + adminId));

        // Verify Admin is in same tenant
        if (!admin.getTenantId().equals(superAdmin.getTenantId())) {
            throw new EntityNotFoundException("Admin not found in your tenant");
        }

        // Verify it's actually an Admin
        Optional<UserRole> role = userRoleRepository.findById(admin.getRoleId());
        if (role.isEmpty() || !"ADMIN".equalsIgnoreCase(role.get().getRole())) {
            throw new EntityNotFoundException("User is not an Admin");
        }

        admin.setIsActive(statusRequest.getIsActive());
        if (Boolean.FALSE.equals(statusRequest.getIsActive())) {
            admin.setAccountLocked(true);
        }

        TenantUser updated = tenantUserRepository.save(admin);
        return convertToDTO(updated);
    }

    // ==================== Helper Methods ====================

    /**
     * Convert TenantUser entity to DTO with related information
     */
    private TenantUserDTO convertToDTO(TenantUser tenantUser) {
        TenantUserDTO dto = TenantUserDTO.builder()
                .tenantUserId(tenantUser.getTenantUserId())
                .tenantId(tenantUser.getTenantId())
                .platformUserId(tenantUser.getPlatformUserId())
                .roleId(tenantUser.getRoleId())
                .tenantUserCode(tenantUser.getTenantUserCode())
                .managerTenantUserId(tenantUser.getManagerTenantUserId())
                .name(tenantUser.getName())
                .email(tenantUser.getEmail())
                .mobileNumber(tenantUser.getMobileNumber())
                .isActive(tenantUser.getIsActive())
                .loginAttempts(tenantUser.getLoginAttempts())
                .accountLocked(tenantUser.getAccountLocked())
                .lastLoginTime(tenantUser.getLastLoginTime())
                .createdAt(tenantUser.getCreatedDate())
                .updatedAt(tenantUser.getModifiedDate())
                .build();

        // Add tenant information
        if (tenantUser.getTenantId() != null) {
            tenantRepository.findById(tenantUser.getTenantId())
                    .ifPresent(tenant -> {
                        dto.setTenantName(tenant.getTenantName());
                        dto.setTenantCode(tenant.getTenantCode());
                    });
        }

        if (tenantUser.getPlatformUserId() != null) {
            platformUserRepository.findById(tenantUser.getPlatformUserId())
                    .ifPresent(platformUser -> {
                        dto.setPlatformUserName(platformUser.getName());
                        dto.setPlatformUserCode(platformUser.getPlatformUserCode());
                    });
        }

        // Add role information
        if (tenantUser.getRoleId() != null) {
            userRoleRepository.findById(tenantUser.getRoleId())
                    .ifPresent(role -> dto.setRole(role.getRole()));
        }

        // Add manager information
        if (tenantUser.getManagerTenantUserId() != null) {
            tenantUserRepository.findById(tenantUser.getManagerTenantUserId())
                    .ifPresent(manager -> dto.setManagerName(manager.getName()));
        }

        return dto;
    }
}
