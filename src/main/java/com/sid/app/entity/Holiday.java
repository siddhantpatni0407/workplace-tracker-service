package com.sid.app.entity;

import com.sid.app.audit.Auditable;
import com.sid.app.enums.HolidayType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "holiday",
        uniqueConstraints = @UniqueConstraint(columnNames = {"holiday_date", "name"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Holiday extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long holidayId;

    @Column(name = "holiday_date", nullable = false)
    private LocalDate holidayDate;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "holiday_type", nullable = false, length = 16)
    private HolidayType holidayType; // MANDATORY / OPTIONAL

    @Column(columnDefinition = "text")
    private String description;
}
