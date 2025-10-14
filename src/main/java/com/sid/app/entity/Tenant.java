package com.sid.app.entity;

import com.sid.app.audit.Auditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tenant")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Tenant extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tenant_id")
    private Long tenantId;

    @Column(name = "tenant_name", nullable = false, unique = true, length = 150)
    private String tenantName;

    @Column(name = "tenant_code", nullable = false, unique = true, length = 20)
    private String tenantCode;

    @Column(name = "app_subscription_id", nullable = false)
    private Long appSubscriptionId = 1L;

    @Column(name = "contact_email", length = 150)
    private String contactEmail;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "subscription_start_date")
    private LocalDateTime subscriptionStartDate;

    @Column(name = "subscription_end_date")
    private LocalDateTime subscriptionEndDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_subscription_id", insertable = false, updatable = false)
    private AppSubscription appSubscription;
}
