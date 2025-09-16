package com.sid.app.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * DTO for UserProfile entity.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserProfileDTO {

    @JsonProperty("userProfileId")
    private Long userProfileId;

    @JsonProperty("userId")
    @NotNull(message = "userId is required")
    private Long userId;

    @JsonProperty("username")
    @Size(min = 3, max = 100, message = "username must be between 3 and 100 characters")
    private String username;

    @JsonProperty("email")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "email can be at most 100 characters")
    private String email;

    @JsonProperty("firstName")
    @Size(max = 50, message = "firstName can be at most 50 characters")
    private String firstName;

    @JsonProperty("lastName")
    @Size(max = 50, message = "lastName can be at most 50 characters")
    private String lastName;

    @JsonProperty("phoneNumber")
    @Pattern(regexp = "^\\+?\\d{7,15}$", message = "phoneNumber must be 7-15 digits, optional leading +")
    private String phoneNumber;

    @JsonProperty("dateOfBirth")
    @Past(message = "dateOfBirth must be in the past")
    private LocalDate dateOfBirth;

    @JsonProperty("gender")
    @Size(max = 32)
    private String gender;

    @JsonProperty("department")
    @Size(max = 100)
    private String department;

    @JsonProperty("position")
    @Size(max = 100)
    private String position;

    @JsonProperty("employeeId")
    @Size(max = 50)
    private String employeeId;

    @JsonProperty("dateOfJoining")
    @PastOrPresent(message = "dateOfJoining cannot be in the future")
    private LocalDate dateOfJoining;

    @JsonProperty("profilePicture")
    private String profilePicture;

    @JsonProperty("bio")
    @Size(max = 500)
    private String bio;

    @JsonProperty("emergencyContactName")
    @Size(max = 100)
    private String emergencyContactName;

    @JsonProperty("emergencyContactPhone")
    @Pattern(regexp = "^\\+?\\d{7,15}$", message = "emergencyContactPhone must be 7-15 digits, optional leading +")
    private String emergencyContactPhone;

    @JsonProperty("emergencyContactRelation")
    @Size(max = 50)
    private String emergencyContactRelation;

    // New: primary address (nested)
    @JsonProperty("primaryAddress")
    @Valid
    private UserAddressDTO primaryAddress;

}