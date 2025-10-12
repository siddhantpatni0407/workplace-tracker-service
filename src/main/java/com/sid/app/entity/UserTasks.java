package com.sid.app.entity;

import com.sid.app.audit.Auditable;
import com.sid.app.enums.TaskCategory;
import com.sid.app.enums.TaskPriority;
import com.sid.app.enums.TaskStatus;
import com.sid.app.enums.TaskType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_tasks",
        indexes = {
                @Index(name = "idx_user_tasks_user_id", columnList = "user_id"),
                @Index(name = "idx_user_tasks_status", columnList = "status"),
                @Index(name = "idx_user_tasks_priority", columnList = "priority"),
                @Index(name = "idx_user_tasks_category", columnList = "category"),
                @Index(name = "idx_user_tasks_task_type", columnList = "task_type"),
                @Index(name = "idx_user_tasks_due_date", columnList = "due_date"),
                @Index(name = "idx_user_tasks_task_date", columnList = "task_date"),
                @Index(name = "idx_user_tasks_created_at", columnList = "created_at"),
                @Index(name = "idx_user_tasks_parent_id", columnList = "parent_task_id"),
                @Index(name = "idx_user_tasks_reminder_date", columnList = "reminder_date")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class UserTasks extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_task_id")
    private Long userTaskId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "task_title", length = 500, nullable = false)
    private String taskTitle;

    @Column(name = "task_description", columnDefinition = "TEXT")
    private String taskDescription;

    @Column(name = "task_date", nullable = false)
    private LocalDate taskDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private TaskStatus status = TaskStatus.NOT_STARTED;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    @Builder.Default
    private TaskPriority priority = TaskPriority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    @Builder.Default
    private TaskCategory category = TaskCategory.WORK;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_type")
    @Builder.Default
    private TaskType taskType = TaskType.TASK;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "reminder_date")
    private LocalDateTime reminderDate;

    @Column(name = "tags", columnDefinition = "TEXT[]")
    private String[] tags;

    @Column(name = "parent_task_id")
    private Long parentTaskId;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "is_recurring")
    @Builder.Default
    private Boolean isRecurring = false;

    @Column(name = "recurring_pattern", length = 100)
    private String recurringPattern;

    @Column(name = "version", nullable = false)
    @Builder.Default
    private Integer version = 1;

    @Column(name = "access_count", nullable = false)
    @Builder.Default
    private Integer accessCount = 0;

    @Column(name = "last_accessed_date")
    private LocalDateTime lastAccessedDate;

    // Foreign key relationship with User entity
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_user_tasks_user"))
    private User user;

    // Self-referencing relationship for parent tasks
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_task_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_user_tasks_parent"))
    private UserTasks parentTask;

    // Creator relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_user_tasks_created_by"))
    private User creator;
}
