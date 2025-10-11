package com.sid.app.controller;

import com.sid.app.auth.JwtAuthenticationContext;
import com.sid.app.auth.RequiredRole;
import com.sid.app.constants.AppConstants;
import com.sid.app.enums.*;
import com.sid.app.model.*;
import com.sid.app.service.UserNotesService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Controller for handling UserNotes-related operations.
 * Provides comprehensive CRUD operations, filtering, search, statistics, and bulk operations for user notes.
 *
 * <p>Author: Siddhant Patni</p>
 */
@RestController
@Slf4j
@CrossOrigin
@RequiredArgsConstructor
public class UserNotesController {

    private final UserNotesService userNotesService;

    @Autowired
    private JwtAuthenticationContext jwtAuthenticationContext;

    // Core CRUD Operations

    /**
     * Create a new note for the authenticated user.
     *
     * @param noteDTO The note data to create
     * @return ResponseEntity with the created note
     */
    @PostMapping(AppConstants.NOTES_ENDPOINT)
    @RequiredRole({"USER"})
    public ResponseEntity<ResponseDTO<UserNotesDTO>> createNote(@RequestBody @Valid UserNotesDTO noteDTO) {
        Long userId = jwtAuthenticationContext.getCurrentUserId();
        log.info("createNote() : Creating note for user: {}", userId);

        try {
            UserNotesDTO createdNote = userNotesService.createNote(userId, noteDTO);
            log.info("createNote() : Note created successfully with ID: {}", createdNote.getUserNoteId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, AppConstants.SUCCESS_NOTE_CREATED, createdNote));
        } catch (Exception e) {
            log.error("createNote() : Error creating note for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "Failed to create note: " + e.getMessage(), null));
        }
    }

    /**
     * Get a specific note by ID for the authenticated user.
     *
     * @param noteId The ID of the note to retrieve
     * @return ResponseEntity with the note data
     */
    @GetMapping(AppConstants.NOTES_ENDPOINT)
    @RequiredRole({"USER"})
    public ResponseEntity<ResponseDTO<UserNotesDTO>> getNoteById(@RequestParam Long noteId) {
        Long userId = jwtAuthenticationContext.getCurrentUserId();
        log.info("getNoteById() : Fetching note {} for user {}", noteId, userId);

        try {
            UserNotesDTO note = userNotesService.getNoteById(userId, noteId);
            return ResponseEntity.ok(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, AppConstants.SUCCESS_NOTE_RETRIEVED, note));
        } catch (EntityNotFoundException e) {
            log.warn("getNoteById() : Note {} not found for user {}", noteId, userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, AppConstants.ERROR_NOTE_NOT_FOUND, null));
        } catch (Exception e) {
            log.error("getNoteById() : Error fetching note {} for user {}: {}", noteId, userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "Failed to retrieve note: " + e.getMessage(), null));
        }
    }

    /**
     * Get all notes for the authenticated user with pagination and optional filters.
     *
     * @param page       Page number (default: 0)
     * @param limit      Page size (default: 20)
     * @param noteType   Filter by note type
     * @param color      Filter by color
     * @param category   Filter by category
     * @param priority   Filter by priority
     * @param status     Filter by status
     * @param isPinned   Filter by pinned status
     * @param isShared   Filter by shared status
     * @param searchTerm Search term for title and content
     * @param sortBy     Sort field (default: modifiedDate)
     * @param sortOrder  Sort order (default: desc)
     * @param startDate  Filter by created date range start
     * @param endDate    Filter by created date range end
     * @return ResponseEntity with paginated notes
     */
    @GetMapping(AppConstants.NOTES_USER_ENDPOINT)
    @RequiredRole({"USER"})
    public ResponseEntity<ResponseDTO<UserNotesListResponseDTO>> getAllUserNotes(@RequestParam(defaultValue = "0") int page,
                                                                                 @RequestParam(defaultValue = "20") int limit,
                                                                                 @RequestParam(required = false) NoteType noteType,
                                                                                 @RequestParam(required = false) NoteColor color,
                                                                                 @RequestParam(required = false) NoteCategory category,
                                                                                 @RequestParam(required = false) NotePriority priority,
                                                                                 @RequestParam(required = false) NoteStatus status,
                                                                                 @RequestParam(required = false) Boolean isPinned,
                                                                                 @RequestParam(required = false) Boolean isShared,
                                                                                 @RequestParam(required = false) String searchTerm,
                                                                                 @RequestParam(defaultValue = "modifiedDate") String sortBy,
                                                                                 @RequestParam(defaultValue = "desc") String sortOrder,
                                                                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
                                                                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        Long userId = jwtAuthenticationContext.getCurrentUserId();
        log.info("getAllUserNotes() : Fetching notes for user {} with filters", userId);

        try {
            Sort.Direction direction = sortOrder.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, limit, Sort.by(direction, sortBy));

            UserNotesListResponseDTO response;
            if (hasFilters(noteType, color, category, priority, status, isPinned, isShared, searchTerm, startDate, endDate)) {
                response = userNotesService.getNotesWithFilters(userId, noteType, color, category, priority,
                        status, isPinned, isShared, startDate, endDate, searchTerm, pageable);
            } else {
                response = userNotesService.getAllUserNotes(userId, pageable);
            }

            if (response.getData().isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, AppConstants.ERROR_NOTES_NOT_FOUND, response));
            }

            return ResponseEntity.ok(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, AppConstants.SUCCESS_NOTES_RETRIEVED, response));
        } catch (Exception e) {
            log.error("getAllUserNotes() : Error fetching notes for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "Failed to retrieve notes: " + e.getMessage(), null));
        }
    }

    /**
     * Update an existing note for the authenticated user.
     *
     * @param noteId  The ID of the note to update
     * @param noteDTO The updated note data
     * @return ResponseEntity with the updated note
     */
    @PutMapping(AppConstants.NOTES_ENDPOINT)
    @RequiredRole({"USER"})
    public ResponseEntity<ResponseDTO<UserNotesDTO>> updateNote(@RequestParam Long noteId,
                                                                @RequestBody @Valid UserNotesDTO noteDTO) {
        Long userId = jwtAuthenticationContext.getCurrentUserId();
        log.info("updateNote() : Updating note {} for user {}", noteId, userId);

        try {
            UserNotesDTO updatedNote = userNotesService.updateNote(userId, noteId, noteDTO);
            return ResponseEntity.ok(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, AppConstants.SUCCESS_NOTE_UPDATED, updatedNote));
        } catch (EntityNotFoundException e) {
            log.warn("updateNote() : Note {} not found for user {}", noteId, userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, AppConstants.ERROR_NOTE_NOT_FOUND, null));
        } catch (Exception e) {
            log.error("updateNote() : Error updating note {} for user {}: {}", noteId, userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "Failed to update note: " + e.getMessage(), null));
        }
    }

    /**
     * Delete a note for the authenticated user.
     *
     * @param noteId    The ID of the note to delete
     * @param permanent Whether to permanently delete the note (default: false)
     * @return ResponseEntity with deletion status
     */
    @DeleteMapping(AppConstants.NOTES_ENDPOINT)
    @RequiredRole({"USER"})
    public ResponseEntity<ResponseDTO<Void>> deleteNote(@RequestParam Long noteId,
                                                        @RequestParam(defaultValue = "false") boolean permanent) {
        Long userId = jwtAuthenticationContext.getCurrentUserId();
        log.info("deleteNote() : Deleting note {} for user {} (permanent: {})", noteId, userId, permanent);

        try {
            userNotesService.deleteNote(userId, noteId, permanent);
            return ResponseEntity.ok(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, AppConstants.SUCCESS_NOTE_DELETED, null));
        } catch (EntityNotFoundException e) {
            log.warn("deleteNote() : Note {} not found for user {}", noteId, userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, AppConstants.ERROR_NOTE_NOT_FOUND, null));
        } catch (Exception e) {
            log.error("deleteNote() : Error deleting note {} for user {}: {}", noteId, userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "Failed to delete note: " + e.getMessage(), null));
        }
    }

    // Filtering & Search Operations

    /**
     * Get notes by specific type for the authenticated user.
     *
     * @param noteType  The type of notes to retrieve
     * @param page      Page number (default: 0)
     * @param limit     Page size (default: 20)
     * @param sortBy    Sort field (default: modifiedDate)
     * @param sortOrder Sort order (default: desc)
     * @return ResponseEntity with filtered notes
     */
    @GetMapping(AppConstants.NOTES_BY_TYPE_ENDPOINT)
    @RequiredRole({"USER"})
    public ResponseEntity<ResponseDTO<UserNotesListResponseDTO>> getNotesByType(@RequestParam NoteType noteType,
                                                                                @RequestParam(defaultValue = "0") int page,
                                                                                @RequestParam(defaultValue = "20") int limit,
                                                                                @RequestParam(defaultValue = "modifiedDate") String sortBy,
                                                                                @RequestParam(defaultValue = "desc") String sortOrder) {

        Long userId = jwtAuthenticationContext.getCurrentUserId();
        log.info("getNotesByType() : Fetching notes by type {} for user {}", noteType, userId);

        try {
            Sort.Direction direction = sortOrder.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, limit, Sort.by(direction, sortBy));

            UserNotesListResponseDTO response = userNotesService.getNotesByType(userId, noteType, pageable);

            if (response.getData().isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, AppConstants.ERROR_NOTES_NOT_FOUND, response));
            }

            return ResponseEntity.ok(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, AppConstants.SUCCESS_NOTES_RETRIEVED, response));
        } catch (Exception e) {
            log.error("getNotesByType() : Error fetching notes by type {} for user {}: {}", noteType, userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "Failed to retrieve notes by type: " + e.getMessage(), null));
        }
    }

    /**
     * Get notes by specific category for the authenticated user.
     *
     * @param category  The category of notes to retrieve
     * @param page      Page number (default: 0)
     * @param limit     Page size (default: 20)
     * @param sortBy    Sort field (default: modifiedDate)
     * @param sortOrder Sort order (default: desc)
     * @return ResponseEntity with filtered notes
     */
    @GetMapping(AppConstants.NOTES_BY_CATEGORY_ENDPOINT)
    @RequiredRole({"USER"})
    public ResponseEntity<ResponseDTO<UserNotesListResponseDTO>> getNotesByCategory(@RequestParam NoteCategory category,
                                                                                    @RequestParam(defaultValue = "0") int page,
                                                                                    @RequestParam(defaultValue = "20") int limit,
                                                                                    @RequestParam(defaultValue = "modifiedDate") String sortBy,
                                                                                    @RequestParam(defaultValue = "desc") String sortOrder) {

        Long userId = jwtAuthenticationContext.getCurrentUserId();
        log.info("getNotesByCategory() : Fetching notes by category {} for user {}", category, userId);

        try {
            Sort.Direction direction = sortOrder.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, limit, Sort.by(direction, sortBy));

            UserNotesListResponseDTO response = userNotesService.getNotesByCategory(userId, category, pageable);

            if (response.getData().isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, AppConstants.ERROR_NOTES_NOT_FOUND, response));
            }

            return ResponseEntity.ok(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, AppConstants.SUCCESS_NOTES_RETRIEVED, response));
        } catch (Exception e) {
            log.error("getNotesByCategory() : Error fetching notes by category {} for user {}: {}", category, userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "Failed to retrieve notes by category: " + e.getMessage(), null));
        }
    }

    /**
     * Get all pinned notes for the authenticated user.
     *
     * @param page      Page number (default: 0)
     * @param limit     Page size (default: 20)
     * @param sortBy    Sort field (default: modifiedDate)
     * @param sortOrder Sort order (default: desc)
     * @return ResponseEntity with pinned notes
     */
    @GetMapping(AppConstants.NOTES_PINNED_ENDPOINT)
    @RequiredRole({"USER"})
    public ResponseEntity<ResponseDTO<UserNotesListResponseDTO>> getPinnedNotes(@RequestParam(defaultValue = "0") int page,
                                                                                @RequestParam(defaultValue = "20") int limit,
                                                                                @RequestParam(defaultValue = "modifiedDate") String sortBy,
                                                                                @RequestParam(defaultValue = "desc") String sortOrder) {

        Long userId = jwtAuthenticationContext.getCurrentUserId();
        log.info("getPinnedNotes() : Fetching pinned notes for user {}", userId);

        try {
            Sort.Direction direction = sortOrder.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, limit, Sort.by(direction, sortBy));

            UserNotesListResponseDTO response = userNotesService.getPinnedNotes(userId, pageable);

            if (response.getData().isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, AppConstants.ERROR_NOTES_NOT_FOUND, response));
            }

            return ResponseEntity.ok(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, AppConstants.SUCCESS_NOTES_RETRIEVED, response));
        } catch (Exception e) {
            log.error("getPinnedNotes() : Error fetching pinned notes for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "Failed to retrieve pinned notes: " + e.getMessage(), null));
        }
    }

    /**
     * Get all archived notes for the authenticated user.
     *
     * @param page      Page number (default: 0)
     * @param limit     Page size (default: 20)
     * @param sortBy    Sort field (default: modifiedDate)
     * @param sortOrder Sort order (default: desc)
     * @return ResponseEntity with archived notes
     */
    @GetMapping(AppConstants.NOTES_ARCHIVED_ENDPOINT)
    @RequiredRole({"USER"})
    public ResponseEntity<ResponseDTO<UserNotesListResponseDTO>> getArchivedNotes(@RequestParam(defaultValue = "0") int page,
                                                                                  @RequestParam(defaultValue = "20") int limit,
                                                                                  @RequestParam(defaultValue = "modifiedDate") String sortBy,
                                                                                  @RequestParam(defaultValue = "desc") String sortOrder) {

        Long userId = jwtAuthenticationContext.getCurrentUserId();
        log.info("getArchivedNotes() : Fetching archived notes for user {}", userId);

        try {
            Sort.Direction direction = sortOrder.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, limit, Sort.by(direction, sortBy));

            UserNotesListResponseDTO response = userNotesService.getArchivedNotes(userId, pageable);

            if (response.getData().isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, AppConstants.ERROR_NOTES_NOT_FOUND, response));
            }

            return ResponseEntity.ok(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, AppConstants.SUCCESS_NOTES_RETRIEVED, response));
        } catch (Exception e) {
            log.error("getArchivedNotes() : Error fetching archived notes for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "Failed to retrieve archived notes: " + e.getMessage(), null));
        }
    }

    /**
     * Search notes by title and content for the authenticated user.
     *
     * @param query     Search query string
     * @param page      Page number (default: 0)
     * @param limit     Page size (default: 20)
     * @param sortBy    Sort field (default: modifiedDate)
     * @param sortOrder Sort order (default: desc)
     * @return ResponseEntity with search results
     */
    @GetMapping(AppConstants.NOTES_SEARCH_ENDPOINT)
    @RequiredRole({"USER"})
    public ResponseEntity<ResponseDTO<UserNotesListResponseDTO>> searchNotes(@RequestParam String query,
                                                                             @RequestParam(defaultValue = "0") int page,
                                                                             @RequestParam(defaultValue = "20") int limit,
                                                                             @RequestParam(defaultValue = "modifiedDate") String sortBy,
                                                                             @RequestParam(defaultValue = "desc") String sortOrder) {

        Long userId = jwtAuthenticationContext.getCurrentUserId();
        log.info("searchNotes() : Searching notes for user {} with query: {}", userId, query);

        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, AppConstants.ERROR_INVALID_NOTE_PARAMS, null));
        }

        try {
            Sort.Direction direction = sortOrder.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, limit, Sort.by(direction, sortBy));

            UserNotesListResponseDTO response = userNotesService.searchNotes(userId, query, pageable);

            if (response.getData().isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, AppConstants.ERROR_NOTES_NOT_FOUND, response));
            }

            return ResponseEntity.ok(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, AppConstants.SUCCESS_NOTES_RETRIEVED, response));
        } catch (Exception e) {
            log.error("searchNotes() : Error searching notes for user {} with query {}: {}", userId, query, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "Failed to search notes: " + e.getMessage(), null));
        }
    }

    // Status and Property Update Operations

    /**
     * Update note status for the authenticated user.
     *
     * @param noteId The ID of the note to update
     * @param status The new status
     * @return ResponseEntity with updated note
     */
    @PatchMapping(AppConstants.NOTES_STATUS_ENDPOINT)
    @RequiredRole({"USER"})
    public ResponseEntity<ResponseDTO<UserNotesDTO>> updateNoteStatus(@RequestParam Long noteId,
                                                                      @RequestParam NoteStatus status) {
        Long userId = jwtAuthenticationContext.getCurrentUserId();
        log.info("updateNoteStatus() : Updating status for note {} to {} for user {}", noteId, status, userId);

        try {
            UserNotesDTO updatedNote = userNotesService.updateNoteStatus(userId, noteId, status);
            return ResponseEntity.ok(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, AppConstants.SUCCESS_NOTE_STATUS_UPDATED, updatedNote));
        } catch (EntityNotFoundException e) {
            log.warn("updateNoteStatus() : Note {} not found for user {}", noteId, userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, AppConstants.ERROR_NOTE_NOT_FOUND, null));
        } catch (Exception e) {
            log.error("updateNoteStatus() : Error updating status for note {} for user {}: {}", noteId, userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "Failed to update note status: " + e.getMessage(), null));
        }
    }

    /**
     * Toggle pin status for a note for the authenticated user.
     *
     * @param noteId The ID of the note to toggle pin status
     * @return ResponseEntity with updated note
     */
    @PatchMapping(AppConstants.NOTES_PIN_ENDPOINT)
    @RequiredRole({"USER"})
    public ResponseEntity<ResponseDTO<UserNotesDTO>> togglePinStatus(@RequestParam Long noteId) {
        Long userId = jwtAuthenticationContext.getCurrentUserId();
        log.info("togglePinStatus() : Toggling pin status for note {} for user {}", noteId, userId);

        try {
            UserNotesDTO updatedNote = userNotesService.togglePinStatus(userId, noteId);
            return ResponseEntity.ok(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, AppConstants.SUCCESS_NOTE_PIN_TOGGLED, updatedNote));
        } catch (EntityNotFoundException e) {
            log.warn("togglePinStatus() : Note {} not found for user {}", noteId, userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, AppConstants.ERROR_NOTE_NOT_FOUND, null));
        } catch (Exception e) {
            log.error("togglePinStatus() : Error toggling pin status for note {} for user {}: {}", noteId, userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "Failed to toggle pin status: " + e.getMessage(), null));
        }
    }

    /**
     * Update note color for the authenticated user.
     *
     * @param noteId The ID of the note to update
     * @param color  The new color
     * @return ResponseEntity with updated note
     */
    @PatchMapping(AppConstants.NOTES_COLOR_ENDPOINT)
    @RequiredRole({"USER"})
    public ResponseEntity<ResponseDTO<UserNotesDTO>> updateNoteColor(@RequestParam Long noteId,
                                                                     @RequestParam NoteColor color) {
        Long userId = jwtAuthenticationContext.getCurrentUserId();
        log.info("updateNoteColor() : Updating color for note {} to {} for user {}", noteId, color, userId);

        try {
            UserNotesDTO updatedNote = userNotesService.updateNoteColor(userId, noteId, color);
            return ResponseEntity.ok(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, AppConstants.SUCCESS_NOTE_COLOR_UPDATED, updatedNote));
        } catch (EntityNotFoundException e) {
            log.warn("updateNoteColor() : Note {} not found for user {}", noteId, userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, AppConstants.ERROR_NOTE_NOT_FOUND, null));
        } catch (Exception e) {
            log.error("updateNoteColor() : Error updating color for note {} for user {}: {}", noteId, userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "Failed to update note color: " + e.getMessage(), null));
        }
    }

    // Statistics

    /**
     * Get note statistics for the authenticated user.
     *
     * @return ResponseEntity with note statistics
     */
    @GetMapping(AppConstants.NOTES_STATS_ENDPOINT)
    @RequiredRole({"USER"})
    public ResponseEntity<ResponseDTO<UserNotesStatsDTO>> getNoteStats() {
        Long userId = jwtAuthenticationContext.getCurrentUserId();
        log.info("getNoteStats() : Fetching note statistics for user {}", userId);

        try {
            UserNotesStatsDTO stats = userNotesService.getNoteStats(userId);
            return ResponseEntity.ok(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, AppConstants.SUCCESS_NOTE_STATS_RETRIEVED, stats));
        } catch (Exception e) {
            log.error("getNoteStats() : Error fetching note statistics for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "Failed to retrieve note statistics: " + e.getMessage(), null));
        }
    }

    // Bulk Operations

    /**
     * Bulk update multiple notes for the authenticated user.
     *
     * @param request Bulk update request containing note IDs and updates
     * @return ResponseEntity with updated notes
     */
    @PutMapping(AppConstants.NOTES_BULK_UPDATE_ENDPOINT)
    @RequiredRole({"USER"})
    public ResponseEntity<ResponseDTO<List<UserNotesDTO>>> bulkUpdateNotes(@RequestBody @Valid UserNotesBulkUpdateRequest request) {
        Long userId = jwtAuthenticationContext.getCurrentUserId();
        log.info("bulkUpdateNotes() : Bulk updating {} notes for user {}",
                request.getNoteIds() != null ? request.getNoteIds().size() : 0, userId);

        if (request.getNoteIds() == null || request.getNoteIds().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, AppConstants.ERROR_INVALID_NOTE_PARAMS, Collections.emptyList()));
        }

        try {
            List<UserNotesDTO> updatedNotes = userNotesService.bulkUpdateNotes(userId, request);
            return ResponseEntity.ok(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, AppConstants.SUCCESS_NOTES_BULK_UPDATED, updatedNotes));
        } catch (EntityNotFoundException e) {
            log.warn("bulkUpdateNotes() : Some notes not found for user {}", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, AppConstants.ERROR_NOTE_NOT_FOUND, Collections.emptyList()));
        } catch (Exception e) {
            log.error("bulkUpdateNotes() : Error bulk updating notes for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "Failed to bulk update notes: " + e.getMessage(), Collections.emptyList()));
        }
    }

    /**
     * Bulk delete multiple notes for the authenticated user.
     *
     * @param request Bulk delete request containing note IDs and delete type
     * @return ResponseEntity with deletion status
     */
    @DeleteMapping(AppConstants.NOTES_BULK_DELETE_ENDPOINT)
    @RequiredRole({"USER"})
    public ResponseEntity<ResponseDTO<Void>> bulkDeleteNotes(@RequestBody @Valid UserNotesBulkDeleteRequest request) {
        Long userId = jwtAuthenticationContext.getCurrentUserId();
        log.info("bulkDeleteNotes() : Bulk deleting {} notes for user {} (permanent: {})",
                request.getNoteIds() != null ? request.getNoteIds().size() : 0, userId, request.getPermanentDelete());

        if (request.getNoteIds() == null || request.getNoteIds().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, AppConstants.ERROR_INVALID_NOTE_PARAMS, null));
        }

        try {
            userNotesService.bulkDeleteNotes(userId, request);
            return ResponseEntity.ok(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, AppConstants.SUCCESS_NOTES_BULK_DELETED, null));
        } catch (EntityNotFoundException e) {
            log.warn("bulkDeleteNotes() : Some notes not found for user {}", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, AppConstants.ERROR_NOTE_NOT_FOUND, null));
        } catch (Exception e) {
            log.error("bulkDeleteNotes() : Error bulk deleting notes for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "Failed to bulk delete notes: " + e.getMessage(), null));
        }
    }

    // Additional Features

    /**
     * Duplicate a note for the authenticated user.
     *
     * @param noteId The ID of the note to duplicate
     * @return ResponseEntity with the duplicated note
     */
    @PostMapping(AppConstants.NOTES_DUPLICATE_ENDPOINT)
    @RequiredRole({"USER"})
    public ResponseEntity<ResponseDTO<UserNotesDTO>> duplicateNote(@RequestParam Long noteId) {
        Long userId = jwtAuthenticationContext.getCurrentUserId();
        log.info("duplicateNote() : Duplicating note {} for user {}", noteId, userId);

        try {
            UserNotesDTO duplicatedNote = userNotesService.duplicateNote(userId, noteId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ResponseDTO<>(AppConstants.STATUS_SUCCESS, AppConstants.SUCCESS_NOTE_DUPLICATED, duplicatedNote));
        } catch (EntityNotFoundException e) {
            log.warn("duplicateNote() : Note {} not found for user {}", noteId, userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, AppConstants.ERROR_NOTE_NOT_FOUND, null));
        } catch (Exception e) {
            log.error("duplicateNote() : Error duplicating note {} for user {}: {}", noteId, userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(AppConstants.STATUS_FAILED, "Failed to duplicate note: " + e.getMessage(), null));
        }
    }

    // Private helper methods

    private boolean hasFilters(NoteType noteType, NoteColor color, NoteCategory category, NotePriority priority,
                               NoteStatus status, Boolean isPinned, Boolean isShared, String searchTerm,
                               LocalDateTime startDate, LocalDateTime endDate) {
        return noteType != null || color != null || category != null || priority != null ||
                status != null || isPinned != null || isShared != null ||
                (searchTerm != null && !searchTerm.trim().isEmpty()) ||
                startDate != null || endDate != null;
    }
}
