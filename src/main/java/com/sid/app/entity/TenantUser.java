package com.sid.app.entity;

import com.sid.app.audit.Auditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tenant_user")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class TenantUser extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tenant_user_id")
    private Long tenantUserId;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "platform_user_id", nullable = false)
    private Long platformUserId;

    @Column(name = "role_id", nullable = false)
    private Long roleId;

    @Column(name = "manager_tenant_user_id")
    private Long managerTenantUserId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "tenant_user_code", nullable = false, unique = true, length = 20)
    private String tenantUserCode;

    @Column(name = "mobile_number", unique = true, length = 20)
    private String mobileNumber;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "password_encryption_key_version", nullable = false)
    private Integer passwordEncryptionKeyVersion = 1;

    @Column(name = "last_login_time")
    private LocalDateTime lastLoginTime;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "login_attempts", nullable = false)
    private Integer loginAttempts = 0;

    @Column(name = "account_locked", nullable = false)
    private Boolean accountLocked = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", insertable = false, updatable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "platform_user_id", insertable = false, updatable = false)
    private PlatformUser platformUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", insertable = false, updatable = false)
    private UserRole role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_tenant_user_id", insertable = false, updatable = false)
    private TenantUser managerTenantUser;
}
