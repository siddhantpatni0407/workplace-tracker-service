package com.sid.app.service;

import com.sid.app.entity.User;
import com.sid.app.entity.UserRole;
import com.sid.app.entity.TenantUser;
import com.sid.app.entity.Tenant;
import com.sid.app.model.UserDTO;
import com.sid.app.model.UserStatusUpdateRequest;
import com.sid.app.repository.UserRepository;
import com.sid.app.repository.UserRoleRepository;
import com.sid.app.repository.TenantUserRepository;
import com.sid.app.repository.TenantRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing users, including retrieval, update, and deletion operations.
 * Updated to support multi-tenant architecture with tenant_user_id relationships.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final TenantUserRepository tenantUserRepository;
    private final TenantRepository tenantRepository;

    /**
     * Get all users with tenant information
     */
    public List<UserDTO> getAllUsers() {
        log.info("Fetching all users from the database with tenant information.");
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            log.warn("No users found in the database.");
        }
        return users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get users by tenant ID
     */
    public List<UserDTO> getUsersByTenantId(Long tenantId) {
        log.info("Fetching users for tenant ID: {}", tenantId);
        List<User> users = userRepository.findByTenantId(tenantId);
        return users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get active users by tenant ID
     */
    public List<UserDTO> getActiveUsersByTenantId(Long tenantId) {
        log.info("Fetching active users for tenant ID: {}", tenantId);
        List<User> users = userRepository.findActiveByTenantId(tenantId);
        return users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public UserDTO getUserById(Long userId) {
        log.info("Fetching user with ID: {}", userId);
        return userRepository.findById(userId)
                .map(this::convertToDTO)
                .orElseThrow(() -> {
                    log.warn("User with ID {} not found.", userId);
                    return new EntityNotFoundException("User not found with ID: " + userId);
                });
    }

    @Transactional
    public UserDTO updateUser(Long userId, UserDTO updatedUserDTO) {
        log.info("Updating user with ID: {}", userId);
        return userRepository.findById(userId)
                .map(user -> {
                    if (updatedUserDTO.getName() == null || updatedUserDTO.getEmail() == null) {
                        throw new IllegalArgumentException("Name and email cannot be null.");
                    }

                    // Update basic user fields
                    user.setName(updatedUserDTO.getName());
                    user.setEmail(updatedUserDTO.getEmail());
                    user.setMobileNumber(updatedUserDTO.getMobileNumber());

                    // If role (string) provided in DTO, map it to role_id
                    String requestedRole = updatedUserDTO.getRole();
                    if (requestedRole != null && !requestedRole.isBlank()) {
                        String normalized = requestedRole.trim();
                        Optional<UserRole> roleOpt = userRoleRepository.findByRole(normalized);
                        if (roleOpt.isEmpty()) {
                            throw new IllegalArgumentException("Invalid role: " + requestedRole);
                        }
                        user.setRoleId(roleOpt.get().getRoleId());
                    }

                    userRepository.save(user);
                    log.info("User with ID {} updated successfully.", userId);
                    return convertToDTO(user);
                })
                .orElseThrow(() -> {
                    log.warn("User with ID {} not found.", userId);
                    return new EntityNotFoundException("User not found with ID: " + userId);
                });
    }

    /**
     * Update user's isActive / isAccountLocked flags (both optional).
     */
    @Transactional
    public UserDTO updateUserStatus(UserStatusUpdateRequest req) {
        Long userId = req.getUserId();
        Optional<User> opt = userRepository.findById(userId);

        User user = opt.orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

        boolean changed = false;

        if (req.getIsActive() != null) {
            user.setIsActive(req.getIsActive());
            changed = true;
        }

        if (req.getIsAccountLocked() != null) {
            user.setAccountLocked(req.getIsAccountLocked());
            changed = true;
        }

        if (changed) {
            userRepository.save(user);
            log.info("User status updated for ID: {}", userId);
        }

        return convertToDTO(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        log.info("Deleting user with ID: {}", userId);
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User not found with ID: " + userId);
        }
        userRepository.deleteById(userId);
        log.info("User with ID {} deleted successfully.", userId);
    }

    /**
     * Search users by name or email within a tenant
     */
    public List<UserDTO> searchUsersByTenant(Long tenantId, String searchTerm) {
        log.info("Searching users in tenant {} with term: {}", tenantId, searchTerm);
        List<User> users = userRepository.searchUsersByTenant(tenantId, searchTerm);
        return users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convert User entity to UserDTO with tenant information
     */
    private UserDTO convertToDTO(User user) {
        UserDTO.UserDTOBuilder builder = UserDTO.builder()
                .userId(user.getUserId())
                .tenantUserId(user.getTenantUserId())
                .name(user.getName())
                .email(user.getEmail())
                .mobileNumber(user.getMobileNumber())
                .roleId(user.getRoleId())
                .lastLoginTime(user.getLastLoginTime())
                .loginAttempts(user.getLoginAttempts())
                .isAccountLocked(user.getAccountLocked())
                .isActive(user.getIsActive())
                .createdDate(user.getCreatedDate())
                .modifiedDate(user.getModifiedDate())
                .username(user.getName()); // For backward compatibility

        // Fetch role name
        if (user.getRoleId() != null) {
            userRoleRepository.findById(user.getRoleId())
                    .ifPresent(role -> builder.role(role.getRole()));
        }

        // Fetch tenant information through tenant_user relationship
        if (user.getTenantUserId() != null) {
            tenantUserRepository.findById(user.getTenantUserId())
                    .ifPresent(tenantUser -> {
                        builder.platformUserId(tenantUser.getPlatformUserId());
                        builder.tenantId(tenantUser.getTenantId());

                        // Fetch tenant name
                        if (tenantUser.getTenantId() != null) {
                            tenantRepository.findById(tenantUser.getTenantId())
                                    .ifPresent(tenant -> builder.tenantName(tenant.getTenantName()));
                        }
                    });
        }

        return builder.build();
    }
}
