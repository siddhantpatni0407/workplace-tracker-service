package com.sid.app.model;

import com.sid.app.enums.NoteCategory;
import com.sid.app.enums.NoteColor;
import com.sid.app.enums.NotePriority;
import com.sid.app.enums.NoteStatus;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserNotesBulkUpdateRequest {
    private List<Long> noteIds;
    private NoteColor color;
    private NoteCategory category;
    private NotePriority priority;
    private NoteStatus status;
    private Boolean isPinned;
}
