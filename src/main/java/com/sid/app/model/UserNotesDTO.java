package com.sid.app.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sid.app.enums.NoteCategory;
import com.sid.app.enums.NoteColor;
import com.sid.app.enums.NotePriority;
import com.sid.app.enums.NoteStatus;
import com.sid.app.enums.NoteType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Schema(description = "User personal note record")
public class UserNotesDTO {

    @JsonProperty("userNoteId")
    @Schema(description = "System-generated note ID", example = "15",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Long userNoteId;

    @JsonProperty("userId")
    @Schema(description = "Owner user ID — set from JWT, do not supply in create requests",
            example = "42", accessMode = Schema.AccessMode.READ_ONLY)
    private Long userId;

    @JsonProperty("noteTitle")
    @NotBlank(message = "Note title is required")
    @Size(max = 500, message = "Note title must not exceed 500 characters")
    @Schema(description = "Short title of the note (max 500 chars)", example = "Meeting agenda",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String noteTitle;

    @JsonProperty("noteContent")
    @NotBlank(message = "Note content is required")
    @Schema(description = "Full note body / content",
            example = "1. Review Q2 targets\n2. HR update\n3. AOB",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String noteContent;

    @JsonProperty("noteType")
    @Builder.Default
    @Schema(description = "Content type of the note", example = "TEXT",
            allowableValues = {"TEXT", "CHECKLIST", "CODE", "MARKDOWN"})
    private NoteType noteType = NoteType.TEXT;

    @JsonProperty("color")
    @Builder.Default
    @Schema(description = "Display colour label", example = "DEFAULT",
            allowableValues = {"DEFAULT", "RED", "BLUE", "GREEN", "YELLOW", "PURPLE", "ORANGE"})
    private NoteColor color = NoteColor.DEFAULT;

    @JsonProperty("category")
    @Builder.Default
    @Schema(description = "Note category", example = "PERSONAL",
            allowableValues = {"PERSONAL", "WORK", "STUDY", "OTHER"})
    private NoteCategory category = NoteCategory.PERSONAL;

    @JsonProperty("priority")
    @Builder.Default
    @Schema(description = "Note priority level", example = "MEDIUM",
            allowableValues = {"LOW", "MEDIUM", "HIGH", "CRITICAL"})
    private NotePriority priority = NotePriority.MEDIUM;

    @JsonProperty("status")
    @Builder.Default
    @Schema(description = "Note status", example = "ACTIVE",
            allowableValues = {"ACTIVE", "ARCHIVED", "DELETED"})
    private NoteStatus status = NoteStatus.ACTIVE;

    @JsonProperty("isPinned")
    @Builder.Default
    @Schema(description = "Whether the note is pinned to the top", example = "false")
    private Boolean isPinned = false;

    @JsonProperty("isShared")
    @Builder.Default
    @Schema(description = "Whether the note is shared with others", example = "false")
    private Boolean isShared = false;

    @JsonProperty("reminderDate")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Optional reminder datetime (yyyy-MM-dd HH:mm:ss)", example = "2026-04-15 09:00:00")
    private LocalDateTime reminderDate;

    @JsonProperty("version")
    @Builder.Default
    @Schema(description = "Optimistic-locking version counter", example = "1",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Integer version = 1;

    @JsonProperty("accessCount")
    @Builder.Default
    @Schema(description = "Number of times this note has been viewed", example = "0",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Integer accessCount = 0;

    @JsonProperty("lastAccessedDate")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Timestamp of last view", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime lastAccessedDate;

    @JsonProperty("createdDate")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Record creation timestamp", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdDate;

    @JsonProperty("modifiedDate")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Record last-modified timestamp", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime modifiedDate;
}
