package com.sid.app.model;

import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserNotesStatsDTO {
    private Long totalNotes;
    private Map<String, Long> notesByType;
    private Map<String, Long> notesByColor;
    private Map<String, Long> notesByCategory;
    private Map<String, Long> notesByPriority;
    private Map<String, Long> notesByStatus;
    private Long pinnedNotes;
    private Long sharedNotes;
    private Long notesWithReminders;
    private List<UserNotesDTO> recentlyModified;
}
