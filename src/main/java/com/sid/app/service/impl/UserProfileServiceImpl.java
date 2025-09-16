package com.sid.app.service.impl;

import com.sid.app.entity.User;
import com.sid.app.entity.UserAddress;
import com.sid.app.entity.UserProfile;
import com.sid.app.model.UserAddressDTO;
import com.sid.app.model.UserProfileDTO;
import com.sid.app.repository.UserAddressRepository;
import com.sid.app.repository.UserProfileRepository;
import com.sid.app.repository.UserRepository;
import com.sid.app.service.UserProfileService;
import com.sid.app.service.validation.UserProfileValidator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final UserAddressRepository addressRepository;
    private final UserProfileValidator validator;

    @Override
    @Transactional(readOnly = true)
    public UserProfileDTO getProfile(Long userId) {
        log.info("getProfile() - start, userId={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.info("getProfile() - user not found userId={}", userId);
                    return new EntityNotFoundException("User not found with userId: " + userId);
                });

        Optional<UserProfile> profileOpt = profileRepository.findByUserId(userId);

        UserProfileDTO dto;
        if (profileOpt.isPresent()) {
            log.info("getProfile() - profile found in user_profile for userId={}", userId);
            dto = toDto(profileOpt.get());
        } else {
            log.info("getProfile() - no user_profile row; building minimal DTO from users table for userId={}", userId);
            dto = UserProfileDTO.builder()
                    .userId(user.getUserId())
                    .username(user.getName())
                    .email(user.getEmail())
                    .phoneNumber(user.getMobileNumber())
                    .build();
        }

        // always source identity fields from users table
        dto.setUsername(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getMobileNumber());

        addressRepository.findByUserIdAndIsPrimaryTrue(userId)
                .ifPresent(addr -> {
                    dto.setPrimaryAddress(toAddressDto(addr));
                    log.info("getProfile() - primary address attached for userId={}", userId);
                });

        log.info("getProfile() - completed for userId={}", userId);
        return dto;
    }

    @Override
    @Transactional
    public UserProfileDTO upsertProfile(UserProfileDTO dto) {
        log.info("upsertProfile() - start for userId={}", dto != null ? dto.getUserId() : null);

        // validation
        validator.validateForUpsert(dto);
        log.info("upsertProfile() - validation passed for userId={}", dto.getUserId());

        final Long userId = dto.getUserId();
        if (userId == null) {
            log.info("upsertProfile() - missing userId in DTO");
            throw new IllegalArgumentException("userId is required");
        }

        // ensure user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.info("upsertProfile() - user not found userId={}", userId);
                    return new EntityNotFoundException("User not found with userId: " + userId);
                });

        // --- Uniqueness checks against users table (not user_profile) ---
        if (dto.getUsername() != null && !dto.getUsername().isBlank()) {
            String candidateName = dto.getUsername().trim();
            Optional<User> userByNameOpt = userRepository.findByName(candidateName);
            if (userByNameOpt.isPresent() && !userByNameOpt.get().getUserId().equals(userId)) {
                log.info("upsertProfile() - username conflict for userId={} candidate={}", userId, candidateName);
                throw new IllegalArgumentException("Username is already taken by another user.");
            }
        }

        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            String candidateEmail = dto.getEmail().trim().toLowerCase();
            Optional<User> userByEmailOpt = userRepository.findByEmail(candidateEmail);
            if (userByEmailOpt.isPresent() && !userByEmailOpt.get().getUserId().equals(userId)) {
                log.info("upsertProfile() - email conflict for userId={} candidate={}", userId, candidateEmail);
                throw new IllegalArgumentException("Email is already used by another user.");
            }
        }

        if (dto.getPhoneNumber() != null && !dto.getPhoneNumber().isBlank()) {
            String candidatePhone = dto.getPhoneNumber().trim();
            Optional<User> userByPhoneOpt = userRepository.findByMobileNumber(candidatePhone);
            if (userByPhoneOpt.isPresent() && !userByPhoneOpt.get().getUserId().equals(userId)) {
                log.info("upsertProfile() - phone conflict for userId={} candidate={}", userId, candidatePhone);
                throw new IllegalArgumentException("Phone number is already used by another user.");
            }
        }

        // If client provided username, email or phoneNumber, update the users table (single source of truth)
        boolean userChanged = false;

        if (dto.getUsername() != null && !dto.getUsername().isBlank()) {
            String newName = dto.getUsername().trim();
            if (!newName.equals(user.getName())) {
                user.setName(newName);
                userChanged = true;
                log.info("upsertProfile() - will update users.name for userId={} -> {}", userId, newName);
            }
        }

        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            String newEmail = dto.getEmail().trim().toLowerCase();
            String existingEmail = user.getEmail();
            if (existingEmail == null || !newEmail.equalsIgnoreCase(existingEmail)) {
                user.setEmail(newEmail);
                userChanged = true;
                log.info("upsertProfile() - will update users.email for userId={} -> {}", userId, newEmail);
            }
        }

        if (dto.getPhoneNumber() != null && !dto.getPhoneNumber().isBlank()) {
            String newPhone = dto.getPhoneNumber().trim();
            String existingPhone = user.getMobileNumber();
            if (existingPhone == null || !newPhone.equals(existingPhone)) {
                user.setMobileNumber(newPhone);
                userChanged = true;
                log.info("upsertProfile() - will update users.mobileNumber for userId={} -> {}", userId, newPhone);
            }
        }

        if (userChanged) {
            userRepository.save(user);
            log.info("upsertProfile() - users table updated for userId={}", userId);
        } else {
            log.info("upsertProfile() - no changes to users table for userId={}", userId);
        }

        // load or create profile entity
        UserProfile entity = profileRepository.findByUserId(userId).orElseGet(() -> {
            log.info("upsertProfile() - creating new UserProfile row for userId={}", userId);
            return UserProfile.builder().userId(userId).build();
        });

        // Apply profile-only fields
        if (dto.getDateOfBirth() != null) entity.setDateOfBirth(dto.getDateOfBirth());
        if (dto.getGender() != null) entity.setGender(dto.getGender().trim());
        if (dto.getDepartment() != null) entity.setDepartment(dto.getDepartment().trim());
        if (dto.getPosition() != null) entity.setPosition(dto.getPosition().trim());
        if (dto.getEmployeeId() != null) entity.setEmployeeId(dto.getEmployeeId().trim());
        if (dto.getDateOfJoining() != null) entity.setDateOfJoining(dto.getDateOfJoining());
        if (dto.getProfilePicture() != null) entity.setProfilePicture(dto.getProfilePicture().trim());
        if (dto.getBio() != null) entity.setBio(dto.getBio().trim());
        if (dto.getEmergencyContactName() != null) entity.setEmergencyContactName(dto.getEmergencyContactName().trim());
        if (dto.getEmergencyContactPhone() != null)
            entity.setEmergencyContactPhone(dto.getEmergencyContactPhone().trim());
        if (dto.getEmergencyContactRelation() != null)
            entity.setEmergencyContactRelation(dto.getEmergencyContactRelation().trim());

        // Save profile
        UserProfile savedProfile = profileRepository.save(entity);
        log.info("upsertProfile() - profile saved userId={}, userProfileId={}", userId, savedProfile.getUserProfileId());

        // handle primaryAddress (same logic as before)
        if (dto.getPrimaryAddress() != null) {
            log.info("upsertProfile() - handling primaryAddress for userId={}", userId);
            UserAddressDTO pad = dto.getPrimaryAddress();
            pad.setUserId(userId);

            Optional<UserAddress> existingPrimaryOpt = addressRepository.findByUserIdAndIsPrimaryTrue(userId);

            if (pad.getUserAddressId() != null) {
                Optional<UserAddress> existingById = addressRepository.findById(pad.getUserAddressId());
                if (existingById.isPresent()) {
                    UserAddress ex = existingById.get();
                    log.info("upsertProfile() - updating existing address id={} for userId={}", ex.getUserAddressId(), userId);
                    if (!Boolean.TRUE.equals(ex.getIsPrimary())) {
                        existingPrimaryOpt.ifPresent(ep -> {
                            ep.setIsPrimary(Boolean.FALSE);
                            addressRepository.save(ep);
                            log.info("upsertProfile() - cleared previous primary address id={} for userId={}", ep.getUserAddressId(), userId);
                        });
                        ex.setIsPrimary(Boolean.TRUE);
                    }
                    ex.setAddress(pad.getAddress());
                    ex.setCity(pad.getCity());
                    ex.setState(pad.getState());
                    ex.setCountry(pad.getCountry());
                    ex.setPostalCode(pad.getPostalCode());
                    addressRepository.save(ex);
                    log.info("upsertProfile() - address updated id={} for userId={}", ex.getUserAddressId(), userId);
                } else {
                    log.info("upsertProfile() - provided primaryAddress id={} not found; creating new primary for userId={}", pad.getUserAddressId(), userId);
                    existingPrimaryOpt.ifPresent(ep -> {
                        ep.setIsPrimary(Boolean.FALSE);
                        addressRepository.save(ep);
                        log.info("upsertProfile() - cleared previous primary address id={} for userId={}", ep.getUserAddressId(), userId);
                    });
                    UserAddress na = UserAddress.builder()
                            .userId(userId)
                            .address(pad.getAddress())
                            .city(pad.getCity())
                            .state(pad.getState())
                            .country(pad.getCountry())
                            .postalCode(pad.getPostalCode())
                            .isPrimary(Boolean.TRUE)
                            .build();
                    addressRepository.save(na);
                    log.info("upsertProfile() - new primary address created for userId={}", userId);
                }
            } else {
                if (existingPrimaryOpt.isPresent()) {
                    UserAddress existing = existingPrimaryOpt.get();
                    log.info("upsertProfile() - updating existing primary address id={} for userId={}", existing.getUserAddressId(), userId);
                    existing.setAddress(pad.getAddress());
                    existing.setCity(pad.getCity());
                    existing.setState(pad.getState());
                    existing.setCountry(pad.getCountry());
                    existing.setPostalCode(pad.getPostalCode());
                    existing.setIsPrimary(Boolean.TRUE);
                    addressRepository.save(existing);
                } else {
                    log.info("upsertProfile() - creating new primary address for userId={}", userId);
                    UserAddress na = UserAddress.builder()
                            .userId(userId)
                            .address(pad.getAddress())
                            .city(pad.getCity())
                            .state(pad.getState())
                            .country(pad.getCountry())
                            .postalCode(pad.getPostalCode())
                            .isPrimary(Boolean.TRUE)
                            .build();
                    addressRepository.save(na);
                }
            }
        } else {
            log.info("upsertProfile() - no primaryAddress provided for userId={}", userId);
        }

        // Build result DTO and override identity fields from users table
        UserProfileDTO result = toDto(savedProfile);

        // Refresh identity fields from users table (they might have been updated above)
        User refreshedUser = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found after update for userId: " + userId));

        result.setUsername(refreshedUser.getName());
        result.setEmail(refreshedUser.getEmail());
        result.setPhoneNumber(refreshedUser.getMobileNumber());

        addressRepository.findByUserIdAndIsPrimaryTrue(userId)
                .ifPresent(addr -> {
                    result.setPrimaryAddress(toAddressDto(addr));
                    log.info("upsertProfile() - result includes primaryAddress id={} for userId={}", addr.getUserAddressId(), userId);
                });

        log.info("upsertProfile() - completed for userId={}, userProfileId={}", userId, savedProfile.getUserProfileId());
        return result;
    }

    private UserAddressDTO toAddressDto(UserAddress a) {
        if (a == null) return null;
        return UserAddressDTO.builder()
                .userAddressId(a.getUserAddressId())
                .userId(a.getUserId())
                .address(a.getAddress())
                .city(a.getCity())
                .state(a.getState())
                .country(a.getCountry())
                .postalCode(a.getPostalCode())
                .isPrimary(a.getIsPrimary())
                .build();
    }

    private UserProfileDTO toDto(UserProfile p) {
        if (p == null) return null;
        return UserProfileDTO.builder()
                .userProfileId(p.getUserProfileId())
                .userId(p.getUserId())
                .dateOfBirth(p.getDateOfBirth())
                .gender(p.getGender())
                .department(p.getDepartment())
                .position(p.getPosition())
                .employeeId(p.getEmployeeId())
                .dateOfJoining(p.getDateOfJoining())
                .profilePicture(p.getProfilePicture())
                .bio(p.getBio())
                .emergencyContactName(p.getEmergencyContactName())
                .emergencyContactPhone(p.getEmergencyContactPhone())
                .emergencyContactRelation(p.getEmergencyContactRelation())
                .build();
    }
}
