package com.sid.app.entity;

import com.sid.app.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email"),
        @UniqueConstraint(columnNames = "mobile_number")
})
@EqualsAndHashCode(callSuper = true)
public class User extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "tenant_user_id", nullable = false)
    private Long tenantUserId; // References the Admin user (from tenant_user table) for USER/MANAGER roles

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @Column(name = "email", length = 30, nullable = false, unique = true)
    private String email;

    @Column(name = "mobile_number", length = 15, nullable = false, unique = true)
    private String mobileNumber;

    @Column(name = "password", length = 50, nullable = false)
    private String password;

    @Column(name = "password_encryption_key_version", nullable = false)
    private Integer passwordEncryptionKeyVersion;

    @Column(name = "role_id", nullable = false)
    private Long roleId;

    @Column(name = "last_login_time")
    private LocalDateTime lastLoginTime;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "login_attempts", nullable = false)
    private Integer loginAttempts = 0;

    @Column(name = "account_locked", nullable = false)
    private Boolean accountLocked = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", insertable = false, updatable = false)
    private UserRole role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_user_id", insertable = false, updatable = false)
    private TenantUser tenantUser; // For USER/MANAGER, this will be the Admin they're mapped to
}
