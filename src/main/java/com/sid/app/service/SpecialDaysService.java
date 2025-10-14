package com.sid.app.service;


import com.sid.app.constants.AppConstants;
import com.sid.app.entity.User;
import com.sid.app.entity.UserAddress;
import com.sid.app.entity.UserProfile;
import com.sid.app.model.CurrentMonthSpecialDaysDTO;
import com.sid.app.model.PaginationDTO;
import com.sid.app.model.ResponseDTO;
import com.sid.app.model.SpecialDayDTO;
import com.sid.app.model.SpecialDaysDataDTO;
import com.sid.app.repository.UserRepository;
import com.sid.app.repository.UserProfileRepository;
import com.sid.app.repository.UserAddressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for handling Special Days operations (birthdays and work anniversaries)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SpecialDaysService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserAddressRepository userAddressRepository;

    /**
     * Get special days with filtering and pagination
     */
    public ResponseDTO<SpecialDaysDataDTO> getSpecialDays(Integer month, Integer year,
                                                          Integer page, Integer limit,
                                                          String type, String department,
                                                          String location) {
        try {
            log.info("Fetching special days with filters: month={}, year={}, type={}, department={}, location={}, page={}, limit={}",
                    month, year, type, department, location, page, limit);

            // Set defaults
            if (year == null) year = Year.now().getValue();
            if (page == null) page = 1;
            if (limit == null) limit = 50;
            if (type == null) type = "all";
            if (department == null) department = "all";
            if (location == null) location = "all";

            // Create pageable
            Pageable pageable = PageRequest.of(page - 1, limit, Sort.by("name").ascending());

            // Get users with USER role only
            Page<User> usersPage = getUsersWithFilters(department, location, pageable);
            log.info("Found {} active users with USER role for special days processing", usersPage.getTotalElements());

            List<SpecialDayDTO> specialDays = new ArrayList<>();

            for (User user : usersPage.getContent()) {
                // Fetch user profile and address separately
                Optional<UserProfile> profileOpt = userProfileRepository.findByUserId(user.getUserId());
                Optional<UserAddress> addressOpt = userAddressRepository.findByUserIdAndIsPrimaryTrue(user.getUserId());

                if (profileOpt.isPresent()) {
                    UserProfile profile = profileOpt.get();
                    UserAddress address = addressOpt.orElse(null);

                    // Add birthday if criteria matches
                    if (shouldIncludeBirthday(type, month, profile.getDateOfBirth())) {
                        SpecialDayDTO employeeData = createEmployeeDTO(user, profile, address);
                        specialDays.add(employeeData);
                        log.debug("Added birthday for USER role - user ID {}: {}", user.getUserId(), user.getName());
                    }
                    // For work anniversary, we'll include the same employee data
                    else if (shouldIncludeAnniversary(type, month, profile.getDateOfJoining(), year)) {
                        SpecialDayDTO employeeData = createEmployeeDTO(user, profile, address);
                        specialDays.add(employeeData);
                        log.debug("Added anniversary for USER role - user ID {}: {}", user.getUserId(), user.getName());
                    }
                } else {
                    log.debug("Skipping user ID {} - no profile found", user.getUserId());
                }
            }

            // Create pagination
            PaginationDTO pagination = PaginationDTO.builder()
                    .currentPage(page)
                    .itemsPerPage(limit)
                    .totalItems(usersPage.getTotalElements())
                    .totalPages(usersPage.getTotalPages())
                    .hasPreviousPage(page > 1)
                    .hasNextPage(page < usersPage.getTotalPages())
                    .build();

            SpecialDaysDataDTO data = SpecialDaysDataDTO.builder()
                    .records(specialDays)
                    .pagination(pagination)
                    .build();

            log.info("Successfully retrieved {} special days", specialDays.size());
            return ResponseDTO.<SpecialDaysDataDTO>builder()
                    .status(AppConstants.STATUS_SUCCESS)
                    .message(AppConstants.SUCCESS_SPECIAL_DAYS_RETRIEVED)
                    .data(data)
                    .build();

        } catch (Exception e) {
            log.error("Error retrieving special days", e);
            return ResponseDTO.<SpecialDaysDataDTO>builder()
                    .status(AppConstants.STATUS_FAILED)
                    .message(AppConstants.ERROR_SPECIAL_DAYS_RETRIEVAL_FAILED)
                    .data(null)
                    .build();
        }
    }

    /**
     * Get current month special days for dashboard
     */
    public ResponseDTO<CurrentMonthSpecialDaysDTO> getCurrentMonthSpecialDays(Integer month, Integer year, Integer limit) {
        try {
            if (month == null) month = LocalDate.now().getMonthValue();
            if (year == null) year = Year.now().getValue();
            if (limit == null) limit = 10;

            log.info("Fetching current month special days for month={}, year={}, limit={}", month, year, limit);

            List<User> users = userRepository.findActiveUsersWithProfiles();

            List<SpecialDayDTO> birthdays = new ArrayList<>();
            List<SpecialDayDTO> anniversaries = new ArrayList<>();

            for (User user : users) {
                // Fetch user profile and address separately
                Optional<UserProfile> profileOpt = userProfileRepository.findByUserId(user.getUserId());
                Optional<UserAddress> addressOpt = userAddressRepository.findByUserIdAndIsPrimaryTrue(user.getUserId());

                if (profileOpt.isPresent()) {
                    UserProfile profile = profileOpt.get();
                    UserAddress address = addressOpt.orElse(null);

                    // Check birthdays
                    if (profile.getDateOfBirth() != null &&
                        profile.getDateOfBirth().getMonthValue() == month) {
                        birthdays.add(createEmployeeDTO(user, profile, address));
                        log.debug("Added birthday for user ID {}: {}", user.getUserId(), user.getName());
                    }

                    // Check anniversaries (only if 1+ years of service)
                    if (profile.getDateOfJoining() != null &&
                        profile.getDateOfJoining().getMonthValue() == month &&
                        profile.getDateOfJoining().getYear() < year) {
                        anniversaries.add(createEmployeeDTO(user, profile, address));
                        log.debug("Added anniversary for user ID {}: {}", user.getUserId(), user.getName());
                    }
                }
            }

            // Limit results and sort by name
            birthdays = birthdays.stream()
                    .sorted((a, b) -> a.getName().compareTo(b.getName()))
                    .limit(limit)
                    .collect(Collectors.toList());

            anniversaries = anniversaries.stream()
                    .sorted((a, b) -> a.getName().compareTo(b.getName()))
                    .limit(limit)
                    .collect(Collectors.toList());

            CurrentMonthSpecialDaysDTO.CountsDTO counts = CurrentMonthSpecialDaysDTO.CountsDTO.builder()
                    .birthdays((long) birthdays.size())
                    .anniversaries((long) anniversaries.size())
                    .total((long) (birthdays.size() + anniversaries.size()))
                    .build();

            CurrentMonthSpecialDaysDTO data = CurrentMonthSpecialDaysDTO.builder()
                    .birthdays(birthdays)
                    .anniversaries(anniversaries)
                    .counts(counts)
                    .build();

            log.info("Successfully retrieved current month special days: {} birthdays, {} anniversaries",
                    birthdays.size(), anniversaries.size());

            return ResponseDTO.<CurrentMonthSpecialDaysDTO>builder()
                    .status(AppConstants.STATUS_SUCCESS)
                    .message(AppConstants.SUCCESS_CURRENT_MONTH_SPECIAL_DAYS_RETRIEVED)
                    .data(data)
                    .build();

        } catch (Exception e) {
            log.error("Error retrieving current month special days", e);
            return ResponseDTO.<CurrentMonthSpecialDaysDTO>builder()
                    .status(AppConstants.STATUS_FAILED)
                    .message(AppConstants.ERROR_CURRENT_MONTH_SPECIAL_DAYS_FAILED)
                    .data(null)
                    .build();
        }
    }

    private Page<User> getUsersWithFilters(String department, String location, Pageable pageable) {
        // For now, returning all active users with profiles
        // This can be enhanced with actual filtering logic when needed
        return userRepository.findActiveUsersWithProfiles(pageable);
    }

    private boolean shouldIncludeBirthday(String type, Integer month, LocalDate dateOfBirth) {
        if (dateOfBirth == null) return false;
        if (!"all".equals(type) && !"birthday".equals(type)) return false;
        if (month != null && dateOfBirth.getMonthValue() != month) return false;
        return true;
    }

    private boolean shouldIncludeAnniversary(String type, Integer month, LocalDate dateOfJoining, Integer year) {
        if (dateOfJoining == null) return false;
        if (!"all".equals(type) && !"work-anniversary".equals(type)) return false;
        if (month != null && dateOfJoining.getMonthValue() != month) return false;
        // Only include if employee has at least 1 year of service
        return dateOfJoining.getYear() < year;
    }

    private SpecialDayDTO createEmployeeDTO(User user, UserProfile profile, UserAddress address) {
        return SpecialDayDTO.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .designation(profile.getPosition())
                .city(address != null ? address.getCity() : null)
                .country(address != null ? address.getCountry() : null)
                .dateOfBirth(profile.getDateOfBirth())
                .dateOfJoining(profile.getDateOfJoining())
                .build();
    }
}
