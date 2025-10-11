package com.sid.app.entity;

import com.sid.app.audit.Auditable;
import com.sid.app.enums.NoteCategory;
import com.sid.app.enums.NoteColor;
import com.sid.app.enums.NotePriority;
import com.sid.app.enums.NoteStatus;
import com.sid.app.enums.NoteType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_notes",
        indexes = {
                @Index(name = "idx_user_id", columnList = "user_id"),
                @Index(name = "idx_note_type", columnList = "note_type"),
                @Index(name = "idx_category", columnList = "category"),
                @Index(name = "idx_status", columnList = "status"),
                @Index(name = "idx_is_pinned", columnList = "is_pinned"),
                @Index(name = "idx_created_date", columnList = "created_date"),
                @Index(name = "idx_modified_date", columnList = "modified_date")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class UserNotes extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_note_id")
    private Long noteId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "note_title", length = 500, nullable = false)
    private String noteTitle;

    @Column(name = "note_content", columnDefinition = "LONGTEXT", nullable = false)
    private String noteContent;

    @Enumerated(EnumType.STRING)
    @Column(name = "note_type", nullable = false)
    @Builder.Default
    private NoteType noteType = NoteType.TEXT;

    @Enumerated(EnumType.STRING)
    @Column(name = "color", nullable = false)
    @Builder.Default
    private NoteColor color = NoteColor.DEFAULT;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    @Builder.Default
    private NoteCategory category = NoteCategory.PERSONAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    @Builder.Default
    private NotePriority priority = NotePriority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private NoteStatus status = NoteStatus.ACTIVE;

    @Column(name = "is_pinned", nullable = false)
    @Builder.Default
    private Boolean isPinned = false;

    @Column(name = "is_shared", nullable = false)
    @Builder.Default
    private Boolean isShared = false;

    @Column(name = "reminder_date")
    private LocalDateTime reminderDate;

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
            foreignKey = @ForeignKey(name = "fk_user_notes_user_id"))
    private User user;
}
