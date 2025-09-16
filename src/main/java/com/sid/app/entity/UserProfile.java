package com.sid.app.entity;

import com.sid.app.audit.Auditable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(
        name = "user_profile",
        uniqueConstraints = {@UniqueConstraint(columnNames = "user_id")},
        indexes = {@Index(name = "idx_user_profile_user_id", columnList = "user_id")}
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class UserProfile extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_profile_id")
    private Long userProfileId;

    @NotNull
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "gender", length = 32)
    private String gender;

    @Column(name = "department", length = 100)
    private String department;

    @Column(name = "position", length = 100)
    private String position;

    @Column(name = "employee_id", length = 50)
    private String employeeId;

    @Column(name = "date_of_joining")
    private LocalDate dateOfJoining;

    @Column(name = "profile_picture", columnDefinition = "TEXT")
    private String profilePicture;

    @Column(name = "bio", length = 500)
    private String bio;

    @Column(name = "emergency_contact_name", length = 100)
    private String emergencyContactName;

    @Column(name = "emergency_contact_phone", length = 20)
    private String emergencyContactPhone;

    @Column(name = "emergency_contact_relation", length = 50)
    private String emergencyContactRelation;

    /**
     * One-to-one mapping to the user's primary address.
     * insertable = false, updatable = false ensures writes happen via UserAddressRepository.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", insertable = false, updatable = false)
    private UserAddress primaryAddress;
}
