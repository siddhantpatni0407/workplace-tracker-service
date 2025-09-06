package com.sid.app.service;

import com.sid.app.entity.User;
import com.sid.app.entity.UserRole;
import com.sid.app.model.UserDTO;
import com.sid.app.model.UserStatusUpdateRequest;
import com.sid.app.repository.UserRepository;
import com.sid.app.repository.UserRoleRepository;
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
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    public List<UserDTO> getAllUsers() {
        log.info("Fetching all users from the database.");
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            log.warn("No users found in the database.");
        }
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
                    if (updatedUserDTO.getUsername() == null || updatedUserDTO.getEmail() == null) {
                        throw new IllegalArgumentException("Username and email cannot be null.");
                    }

                    // map fields
                    user.setName(updatedUserDTO.getUsername());
                    user.setEmail(updatedUserDTO.getEmail());
                    user.setMobileNumber(updatedUserDTO.getMobileNumber());

                    // If role (string) provided in DTO, map it to role_id
                    String requestedRole = updatedUserDTO.getRole();
                    if (requestedRole != null && !requestedRole.isBlank()) {
                        // Normalize role value (e.g., uppercase) depending on how roles are stored
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
            log.info("updateUserStatus() : Updated userId={} isActive={} accountLocked={}",
                    userId, user.getIsActive(), user.getAccountLocked());
        } else {
            log.info("updateUserStatus() : No changes requested for userId={}", userId);
        }

        return convertToDTO(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        log.info("Attempting to delete user with ID: {}", userId);
        if (!userRepository.existsById(userId)) {
            log.warn("User with ID {} not found, deletion aborted.", userId);
            throw new EntityNotFoundException("User not found with ID: " + userId);
        }
        userRepository.deleteById(userId);
        log.info("User with ID {} deleted successfully.", userId);
    }

    private UserDTO convertToDTO(User user) {
        // find role name from roleId; if not found, fallback to null or empty string
        String roleName = null;
        if (user.getRoleId() != null) {
            roleName = userRoleRepository.findById(user.getRoleId())
                    .map(UserRole::getRole)
                    .orElse(null);
        }

        return UserDTO.builder()
                .userId(user.getUserId())
                .username(user.getName())
                .email(user.getEmail())
                .mobileNumber(user.getMobileNumber())
                .role(roleName)                       // <-- still returns role name string
                .lastLoginTime(user.getLastLoginTime())
                .loginAttempts(user.getLoginAttempts())
                .isAccountLocked(user.getAccountLocked())
                .isActive(user.getIsActive())
                .build();
    }

}
