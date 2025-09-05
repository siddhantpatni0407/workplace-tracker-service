package com.sid.app.service;

import com.sid.app.entity.User;
import com.sid.app.model.UserDTO;
import com.sid.app.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing users, including retrieval, update, and deletion operations.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

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
                    user.setName(updatedUserDTO.getUsername());
                    user.setEmail(updatedUserDTO.getEmail());
                    user.setMobileNumber(updatedUserDTO.getMobileNumber());
                    user.setRole(updatedUserDTO.getRole());

                    userRepository.save(user);
                    log.info("User with ID {} updated successfully.", userId);
                    return convertToDTO(user);
                })
                .orElseThrow(() -> {
                    log.warn("User with ID {} not found.", userId);
                    return new EntityNotFoundException("User not found with ID: " + userId);
                });
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
        return UserDTO.builder()
                .userId(user.getUserId())
                .username(user.getName())
                .email(user.getEmail())
                .mobileNumber(user.getMobileNumber())
                .role(user.getRole())
                .lastLoginTime(user.getLastLoginTime())
                .loginAttempts(user.getLoginAttempts())
                .isAccountLocked(user.getAccountLocked())
                .isActive(user.getIsActive())
                .build();
    }

}