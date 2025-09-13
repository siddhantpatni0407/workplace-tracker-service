package com.sid.app.entity;

import com.sid.app.audit.Auditable;
import com.sid.app.enums.VisitType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "office_visit", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "visit_date"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class OfficeVisit extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long officeVisitId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "visit_date", nullable = false)
    private LocalDate visitDate;

    @Column(name = "day_of_week", nullable = false)
    private Integer dayOfWeek;

    @Enumerated(EnumType.STRING)
    @Column(name = "visit_type", nullable = false, length = 32)
    private VisitType visitType;

    @Column(columnDefinition = "text")
    private String notes;
}
