package com.sid.app.service.validation;

import com.sid.app.exception.UserProfileValidationException;
import com.sid.app.model.UserAddressDTO;
import com.sid.app.model.UserProfileDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Component
public class UserProfileValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);

    // accept 7-15 digits with optional leading +
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?\\d{7,15}$");

    /**
     * Allow Unicode letters, digits, spaces, dot, apostrophe, underscore, hyphen.
     * This is suitable for a "display name" like "Sidd Jain" or "O'Connor".
     * <p>
     * If you want `username` to be a login handle (no spaces), revert to the old stricter pattern.
     */
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[\\p{L}0-9 .'_\\-]{3,100}$");

    private static final int MIN_AGE_YEARS = 13;
    private static final int MAX_BIO_LENGTH = 500;

    public void validateForUpsert(UserProfileDTO dto) {
        Map<String, String> errors = new LinkedHashMap<>();

        // userId is required for upsert
        if (dto.getUserId() == null) {
            errors.put("userId", "userId is required");
        }

        // username (display name)
        if (dto.getUsername() != null && !dto.getUsername().isBlank()) {
            String username = dto.getUsername().trim();
            if (username.length() < 3 || username.length() > 100 || !USERNAME_PATTERN.matcher(username).matches()) {
                errors.put("username", "Must be 3-100 chars; allowed: letters, numbers, spaces, dot, apostrophe, underscore, hyphen.");
            }
        }

        // email
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            String email = dto.getEmail().trim();
            if (email.length() > 100) {
                errors.put("email", "Email must not exceed 100 characters.");
            } else if (!EMAIL_PATTERN.matcher(email).matches()) {
                errors.put("email", "Invalid email format.");
            }
        }

        // phone
        if (dto.getPhoneNumber() != null && !dto.getPhoneNumber().isBlank()) {
            String phone = dto.getPhoneNumber().trim();
            if (!PHONE_PATTERN.matcher(phone).matches()) {
                errors.put("phoneNumber", "Invalid phone number. Expect 7-15 digits, optional leading +.");
            }
        }

        // emergency contact phone
        if (dto.getEmergencyContactPhone() != null && !dto.getEmergencyContactPhone().isBlank()) {
            String ephone = dto.getEmergencyContactPhone().trim();
            if (!PHONE_PATTERN.matcher(ephone).matches()) {
                errors.put("emergencyContactPhone", "Invalid emergency contact phone. Expect 7-15 digits, optional leading +.");
            }
        }

        // bio length
        if (dto.getBio() != null && dto.getBio().length() > MAX_BIO_LENGTH) {
            errors.put("bio", "Bio must not exceed " + MAX_BIO_LENGTH + " characters.");
        }

        // name lengths (keep these if DTO still contains firstName/lastName)
        if (dto.getFirstName() != null && dto.getFirstName().trim().length() > 50) {
            errors.put("firstName", "firstName must not exceed 50 characters.");
        }
        if (dto.getLastName() != null && dto.getLastName().trim().length() > 50) {
            errors.put("lastName", "lastName must not exceed 50 characters.");
        }
        if (dto.getEmployeeId() != null && dto.getEmployeeId().trim().length() > 50) {
            errors.put("employeeId", "employeeId must not exceed 50 characters.");
        }

        // emergency contact fields
        if (dto.getEmergencyContactName() != null && dto.getEmergencyContactName().length() > 100) {
            errors.put("emergencyContactName", "emergencyContactName must not exceed 100 characters.");
        }
        if (dto.getEmergencyContactRelation() != null && dto.getEmergencyContactRelation().length() > 50) {
            errors.put("emergencyContactRelation", "emergencyContactRelation must not exceed 50 characters.");
        }

        // date validations
        LocalDate today = LocalDate.now();
        if (dto.getDateOfBirth() != null) {
            LocalDate dob = dto.getDateOfBirth();
            if (dob.isAfter(today)) {
                errors.put("dateOfBirth", "dateOfBirth cannot be in the future.");
            } else {
                int age = Period.between(dob, today).getYears();
                if (age < MIN_AGE_YEARS) {
                    errors.put("dateOfBirth", "User must be at least " + MIN_AGE_YEARS + " years old.");
                }
            }
        }

        if (dto.getDateOfJoining() != null) {
            LocalDate doj = dto.getDateOfJoining();
            if (doj.isAfter(today)) {
                errors.put("dateOfJoining", "dateOfJoining cannot be in the future.");
            }
            if (dto.getDateOfBirth() != null) {
                LocalDate dob = dto.getDateOfBirth();
                if (doj.isBefore(dob)) {
                    errors.put("dateOfJoining", "dateOfJoining cannot be before dateOfBirth.");
                }
            }
        }

        // nested primaryAddress validation
        UserAddressDTO addr = dto.getPrimaryAddress();
        if (addr != null) {
            if (addr.getAddress() != null && addr.getAddress().length() > 10000) {
                errors.put("primaryAddress.address", "address is too long");
            }
            if (addr.getCity() != null && addr.getCity().length() > 100) {
                errors.put("primaryAddress.city", "city can be at most 100 characters");
            }
            if (addr.getState() != null && addr.getState().length() > 100) {
                errors.put("primaryAddress.state", "state can be at most 100 characters");
            }
            if (addr.getCountry() != null && addr.getCountry().length() > 100) {
                errors.put("primaryAddress.country", "country can be at most 100 characters");
            }
            if (addr.getPostalCode() != null && addr.getPostalCode().length() > 30) {
                errors.put("primaryAddress.postalCode", "postalCode can be at most 30 characters");
            }
        }

        if (!errors.isEmpty()) {
            throw new UserProfileValidationException(errors);
        }
    }
}
