package com.sid.app.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("userNoteId")
    private Long userNoteId;

    @JsonProperty("userId")
    private Long userId;

    @JsonProperty("noteTitle")
    @NotBlank(message = "Note title is required")
    @Size(max = 500, message = "Note title must not exceed 500 characters")
    private String noteTitle;

    @JsonProperty("noteContent")
    @NotBlank(message = "Note content is required")
    private String noteContent;

    @JsonProperty("noteType")
    @Builder.Default
    private NoteType noteType = NoteType.TEXT;

    @JsonProperty("color")
    @Builder.Default
    private NoteColor color = NoteColor.DEFAULT;

    @JsonProperty("category")
    @Builder.Default
    private NoteCategory category = NoteCategory.PERSONAL;

    @JsonProperty("priority")
    @Builder.Default
    private NotePriority priority = NotePriority.MEDIUM;

    @JsonProperty("status")
    @Builder.Default
    private NoteStatus status = NoteStatus.ACTIVE;

    @JsonProperty("isPinned")
    @Builder.Default
    private Boolean isPinned = false;

    @JsonProperty("isShared")
    @Builder.Default
    private Boolean isShared = false;

    @JsonProperty("reminderDate")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reminderDate;

    @JsonProperty("version")
    @Builder.Default
    private Integer version = 1;

    @JsonProperty("accessCount")
    @Builder.Default
    private Integer accessCount = 0;

    @JsonProperty("lastAccessedDate")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastAccessedDate;

    @JsonProperty("createdDate")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdDate;

    @JsonProperty("modifiedDate")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime modifiedDate;
}
