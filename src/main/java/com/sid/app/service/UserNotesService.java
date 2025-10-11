package com.sid.app.service;

import com.sid.app.enums.NoteCategory;
import com.sid.app.enums.NoteColor;
import com.sid.app.enums.NotePriority;
import com.sid.app.enums.NoteStatus;
import com.sid.app.enums.NoteType;
import com.sid.app.model.*;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface UserNotesService {

    // Core CRUD operations
    UserNotesDTO createNote(Long userId, UserNotesDTO noteDTO);
    UserNotesDTO getNoteById(Long userId, Long noteId);
    UserNotesListResponseDTO getAllUserNotes(Long userId, Pageable pageable);
    UserNotesDTO updateNote(Long userId, Long noteId, UserNotesDTO noteDTO);
    void deleteNote(Long userId, Long noteId, boolean permanent);

    // Filtering and search
    UserNotesListResponseDTO getNotesWithFilters(Long userId, NoteType noteType, NoteColor color,
                                          NoteCategory category, NotePriority priority,
                                          NoteStatus status, Boolean isPinned, Boolean isShared,
                                          LocalDateTime startDate, LocalDateTime endDate,
                                          String searchTerm, Pageable pageable);

    UserNotesListResponseDTO getNotesByType(Long userId, NoteType noteType, Pageable pageable);
    UserNotesListResponseDTO getNotesByCategory(Long userId, NoteCategory category, Pageable pageable);
    UserNotesListResponseDTO getPinnedNotes(Long userId, Pageable pageable);
    UserNotesListResponseDTO getArchivedNotes(Long userId, Pageable pageable);
    UserNotesListResponseDTO searchNotes(Long userId, String searchTerm, Pageable pageable);

    // Status and property updates
    UserNotesDTO updateNoteStatus(Long userId, Long noteId, NoteStatus status);
    UserNotesDTO togglePinStatus(Long userId, Long noteId);
    UserNotesDTO updateNoteColor(Long userId, Long noteId, NoteColor color);

    // Statistics
    UserNotesStatsDTO getNoteStats(Long userId);

    // Bulk operations
    List<UserNotesDTO> bulkUpdateNotes(Long userId, UserNotesBulkUpdateRequest request);
    void bulkDeleteNotes(Long userId, UserNotesBulkDeleteRequest request);

    // Additional features
    UserNotesDTO duplicateNote(Long userId, Long noteId);
}
