package com.sid.app.entity;

import com.sid.app.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "user_leave_balance", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "policy_id", "year"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class UserLeaveBalance extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userLeaveBalanceId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "policy_id", nullable = false)
    private Long policyId;

    @Column(nullable = false)
    private Integer year;

    @Column(name = "allocated_days", precision = 6, scale = 2, nullable = false)
    private BigDecimal allocatedDays;

    @Column(name = "used_days", precision = 6, scale = 2, nullable = false)
    private BigDecimal usedDays;

    @Column(name = "remaining_days", precision = 6, scale = 2, nullable = false)
    private BigDecimal remainingDays;
}
