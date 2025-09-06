package com.sid.app.entity;

import com.sid.app.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "user_settings", uniqueConstraints = {@UniqueConstraint(columnNames = "user_id")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class UserSettings extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_setting_id")
    private Long userSettingId;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "timezone", length = 64, nullable = false)
    private String timezone = "UTC";

    @Column(name = "work_week_start", nullable = false)
    private Integer workWeekStart = 1; // 1 = Monday

    @Column(name = "language", length = 16, nullable = false)
    private String language = "en";

    @Column(name = "date_format", length = 32, nullable = false)
    private String dateFormat = "yyyy-MM-dd";

}
