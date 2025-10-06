package com.sid.app.entity;

import com.sid.app.audit.Auditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Entity class representing a daily task in the system.
 *
 * <p>Author: Siddhant Patni</p>
 */
@Entity
@Table(name = "daily_tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DailyTask extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "daily_task_id")
    private Long dailyTaskId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "daily_task_date", nullable = false)
    private LocalDate dailyTaskDate;

    @Column(name = "daily_task_day")
    private String dailyTaskDay;

    @Column(name = "task_number", nullable = false)
    private String taskNumber;

    @Column(name = "project_code")
    private String projectCode;

    @Column(name = "project_name")
    private String projectName;

    @Column(name = "story_task_bug_number")
    private String storyTaskBugNumber;

    @Column(name = "task_details")
    private String taskDetails;

    @Column(name = "remarks")
    private String remarks;
}
