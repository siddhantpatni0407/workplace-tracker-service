package com.sid.app.entity;

import com.sid.app.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "leave_policy")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class LeavePolicy extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long policyId;

    @Column(name = "policy_code", nullable = false, unique = true, length = 50)
    private String policyCode;

    @Column(name = "policy_name", nullable = false, length = 100)
    private String policyName;

    @Builder.Default
    @Column(name = "default_annual_days", nullable = false)
    private Integer defaultAnnualDays = 0;

    @Column(columnDefinition = "text")
    private String description;
}
