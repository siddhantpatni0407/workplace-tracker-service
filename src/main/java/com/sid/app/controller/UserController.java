package com.sid.app.controller;

import com.sid.app.constants.AppConstants;
import com.sid.app.model.ResponseDTO;
import com.sid.app.model.UserDTO;
import com.sid.app.service.UserService;
import com.sid.app.utils.ApplicationUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * Controller for handling user-related operations.
 * Provides endpoints for fetching, updating, and deleting users.
 *
 * <p>Author: Siddhant Patni</p>
 */
@RestController
@Slf4j
@CrossOrigin
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * Fetches all users from the system.
     *
     * @return ResponseEntity with a ResponseDTO containing a list of UserDTOs.
     */
    @GetMapping(AppConstants.FETCH_ALL_USERS_ENDPOINT)
    public ResponseEntity<ResponseDTO<List<UserDTO>>> getAllUsers() {
        log.info("getAllUsers() : Received request to fetch all users.");

        List<UserDTO> users = userService.getAllUsers();

        if (users.isEmpty()) {
            log.warn("getAllUsers() : No users found.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO<>("FAILURE", "No users found in the system.", Collections.emptyList()));
        }

        log.info("getAllUsers() : Retrieved {} users. users -> {}", users.size(), ApplicationUtils.getJSONString(users));
        return ResponseEntity.ok(new ResponseDTO<>("SUCCESS", "Users retrieved successfully.", users));
    }

    /**
     * Fetches a user by their ID.
     *
     * @param userId The ID of the user to retrieve.
     * @return ResponseEntity with a ResponseDTO containing the UserDTO.
     */
    @GetMapping(AppConstants.USER_ENDPOINT)
    public ResponseEntity<ResponseDTO<UserDTO>> getUserById(@RequestParam("userId") Long userId) {
        log.info("getUserById() : Received request to fetch user with ID: {}", userId);
        try {
            UserDTO user = userService.getUserById(userId);
            log.info("getUserById() : User with ID {} retrieved successfully.", userId);
            return ResponseEntity.ok(new ResponseDTO<>("SUCCESS", "User retrieved successfully.", user));
        } catch (EntityNotFoundException e) {
            log.warn("getUserById() : User with ID {} not found.", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO<>("FAILED", e.getMessage(), null));
        }
    }

    /**
     * Updates an existing user's details.
     *
     * @param userId         The ID of the user to update.
     * @param updatedUserDTO The updated user details.
     * @return ResponseEntity with a ResponseDTO indicating the update status.
     */
    @PutMapping(AppConstants.USER_ENDPOINT)
    public ResponseEntity<ResponseDTO<UserDTO>> updateUser(@RequestParam("userId") Long userId,
                                                           @RequestBody UserDTO updatedUserDTO) {
        log.info("updateUser() : Received request to update user with ID: {}", userId);
        try {
            UserDTO updatedUser = userService.updateUser(userId, updatedUserDTO);
            log.info("updateUser() : User with ID {} updated successfully.", userId);
            return ResponseEntity.ok(new ResponseDTO<>("SUCCESS", "User updated successfully.", updatedUser));
        } catch (EntityNotFoundException e) {
            log.warn("updateUser() : User with ID {} not found.", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO<>("FAILED", e.getMessage(), null));
        } catch (IllegalArgumentException e) {
            log.warn("updateUser() : Validation failed for user ID {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseDTO<>("FAILED", e.getMessage(), null));
        }
    }

    /**
     * Deletes a user by their ID.
     *
     * @param userId The ID of the user to delete.
     * @return ResponseEntity with a ResponseDTO indicating the deletion status.
     */
    @DeleteMapping(AppConstants.USER_ENDPOINT)
    public ResponseEntity<ResponseDTO<Void>> deleteUser(@RequestParam("userId") Long userId) {
        log.info("deleteUser() : Received request to delete user with ID: {}", userId);
        try {
            userService.deleteUser(userId);
            log.info("deleteUser() : User with ID {} deleted successfully.", userId);
            return ResponseEntity.ok(new ResponseDTO<>("SUCCESS", "User deleted successfully.", null));
        } catch (EntityNotFoundException e) {
            log.warn("deleteUser() : User with ID {} not found.", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO<>("FAILED", e.getMessage(), null));
        }
    }

}