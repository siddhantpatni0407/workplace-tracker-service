package com.sid.app.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sid.app.enums.NoteCategory;
import com.sid.app.enums.NoteColor;
import com.sid.app.enums.NotePriority;
import com.sid.app.enums.NoteStatus;
import com.sid.app.enums.NoteType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UserNotesDTO {

    private Long noteId;

    private Long userId;

    @NotBlank(message = "Note title is required")
    @Size(max = 500, message = "Note title must not exceed 500 characters")
    private String noteTitle;

    @NotBlank(message = "Note content is required")
    private String noteContent;

    @Builder.Default
    private NoteType noteType = NoteType.TEXT;

    @Builder.Default
    private NoteColor color = NoteColor.DEFAULT;

    @Builder.Default
    private NoteCategory category = NoteCategory.PERSONAL;

    @Builder.Default
    private NotePriority priority = NotePriority.MEDIUM;

    @Builder.Default
    private NoteStatus status = NoteStatus.ACTIVE;

    @Builder.Default
    private Boolean isPinned = false;

    @Builder.Default
    private Boolean isShared = false;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reminderDate;

    @Builder.Default
    private Integer version = 1;

    @Builder.Default
    private Integer accessCount = 0;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastAccessedDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime modifiedDate;
}
