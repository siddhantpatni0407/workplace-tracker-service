package com.sid.app.service.impl;

import com.sid.app.entity.UserNotes;
import com.sid.app.enums.*;
import com.sid.app.model.*;
import com.sid.app.repository.UserNotesRepository;
import com.sid.app.service.UserNotesService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class UserNotesServiceImpl implements UserNotesService {

    private final UserNotesRepository userNotesRepository;

    @Override
    public UserNotesDTO createNote(Long userId, UserNotesDTO noteDTO) {
        log.info("Creating note for user: {}", userId);

        UserNotes note = UserNotes.builder()
                .userId(userId)
                .noteTitle(noteDTO.getNoteTitle())
                .noteContent(noteDTO.getNoteContent())
                .noteType(noteDTO.getNoteType() != null ? noteDTO.getNoteType() : NoteType.TEXT)
                .color(noteDTO.getColor() != null ? noteDTO.getColor() : NoteColor.DEFAULT)
                .category(noteDTO.getCategory() != null ? noteDTO.getCategory() : NoteCategory.PERSONAL)
                .priority(noteDTO.getPriority() != null ? noteDTO.getPriority() : NotePriority.MEDIUM)
                .status(noteDTO.getStatus() != null ? noteDTO.getStatus() : NoteStatus.ACTIVE)
                .isPinned(noteDTO.getIsPinned() != null ? noteDTO.getIsPinned() : false)
                .isShared(noteDTO.getIsShared() != null ? noteDTO.getIsShared() : false)
                .reminderDate(noteDTO.getReminderDate())
                .version(1)
                .accessCount(0)
                .build();

        UserNotes savedNote = userNotesRepository.save(note);
        log.info("Note created successfully with ID: {}", savedNote.getUserNoteId());
        return convertToDTO(savedNote);
    }

    @Override
    @Transactional(readOnly = true)
    public UserNotesDTO getNoteById(Long userId, Long noteId) {
        log.info("Fetching note {} for user {}", noteId, userId);

        UserNotes note = userNotesRepository.findByUserNoteIdAndUserId(noteId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Note not found with id: " + noteId));

        // Update access count and last accessed date
        userNotesRepository.updateAccessInfo(noteId, userId, LocalDateTime.now());

        return convertToDTO(note);
    }

    @Override
    @Transactional(readOnly = true)
    public UserNotesListResponseDTO getAllUserNotes(Long userId, Pageable pageable) {
        log.info("Fetching all notes for user: {} with pagination: {}", userId, pageable);

        Page<UserNotes> notesPage = userNotesRepository.findByUserIdAndStatusNot(userId, NoteStatus.DELETED, pageable);

        List<UserNotesDTO> noteDTOs = notesPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        PaginationDTO pagination = PaginationDTO.builder()
                .currentPage(pageable.getPageNumber() + 1)
                .itemsPerPage(pageable.getPageSize())
                .totalItems(notesPage.getTotalElements())
                .totalPages(notesPage.getTotalPages())
                .hasPreviousPage(pageable.getPageNumber() > 0)
                .hasNextPage(pageable.getPageNumber() + 1 < notesPage.getTotalPages())
                .build();

        return UserNotesListResponseDTO.builder()
                .data(noteDTOs)
                .pagination(pagination)
                .build();
    }

    @Override
    public UserNotesDTO updateNote(Long userId, Long noteId, UserNotesDTO noteDTO) {
        log.info("Updating note {} for user {}", noteId, userId);

        UserNotes existingNote = userNotesRepository.findByUserNoteIdAndUserId(noteId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Note not found with id: " + noteId));

        // Update fields
        existingNote.setNoteTitle(noteDTO.getNoteTitle());
        existingNote.setNoteContent(noteDTO.getNoteContent());
        if (noteDTO.getNoteType() != null) existingNote.setNoteType(noteDTO.getNoteType());
        if (noteDTO.getColor() != null) existingNote.setColor(noteDTO.getColor());
        if (noteDTO.getCategory() != null) existingNote.setCategory(noteDTO.getCategory());
        if (noteDTO.getPriority() != null) existingNote.setPriority(noteDTO.getPriority());
        if (noteDTO.getStatus() != null) existingNote.setStatus(noteDTO.getStatus());
        if (noteDTO.getIsPinned() != null) existingNote.setIsPinned(noteDTO.getIsPinned());
        if (noteDTO.getIsShared() != null) existingNote.setIsShared(noteDTO.getIsShared());
        existingNote.setReminderDate(noteDTO.getReminderDate());
        existingNote.setVersion(existingNote.getVersion() + 1);

        UserNotes updatedNote = userNotesRepository.save(existingNote);
        log.info("Note updated successfully: {}", noteId);
        return convertToDTO(updatedNote);
    }

    @Override
    public void deleteNote(Long userId, Long noteId, boolean permanent) {
        log.info("Deleting note {} for user {} (permanent: {})", noteId, userId, permanent);

        UserNotes note = userNotesRepository.findByUserNoteIdAndUserId(noteId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Note not found with id: " + noteId));

        if (permanent) {
            userNotesRepository.delete(note);
            log.info("Note permanently deleted: {}", noteId);
        } else {
            note.setStatus(NoteStatus.DELETED);
            userNotesRepository.save(note);
            log.info("Note soft deleted: {}", noteId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserNotesListResponseDTO getNotesWithFilters(Long userId, NoteType noteType, NoteColor color,
                                                  NoteCategory category, NotePriority priority,
                                                  NoteStatus status, Boolean isPinned, Boolean isShared,
                                                  LocalDateTime startDate, LocalDateTime endDate,
                                                  String searchTerm, Pageable pageable) {
        log.info("Fetching filtered notes for user: {}", userId);

        Page<UserNotes> notesPage;

        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            notesPage = userNotesRepository.searchNotes(userId, searchTerm.trim(), NoteStatus.DELETED, pageable);
        } else {
            notesPage = userNotesRepository.findNotesWithFilters(userId, noteType, color, category,
                    priority, status, isPinned, isShared, startDate, endDate, pageable);
        }

        return buildNoteListResponse(notesPage, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public UserNotesListResponseDTO getNotesByType(Long userId, NoteType noteType, Pageable pageable) {
        log.info("Fetching notes by type {} for user: {}", noteType, userId);

        Page<UserNotes> notesPage = userNotesRepository.findByUserIdAndNoteTypeAndStatusNot(userId, noteType, NoteStatus.DELETED, pageable);
        return buildNoteListResponse(notesPage, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public UserNotesListResponseDTO getNotesByCategory(Long userId, NoteCategory category, Pageable pageable) {
        log.info("Fetching notes by category {} for user: {}", category, userId);

        Page<UserNotes> notesPage = userNotesRepository.findByUserIdAndCategoryAndStatusNot(userId, category, NoteStatus.DELETED, pageable);
        return buildNoteListResponse(notesPage, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public UserNotesListResponseDTO getPinnedNotes(Long userId, Pageable pageable) {
        log.info("Fetching pinned notes for user: {}", userId);

        Page<UserNotes> notesPage = userNotesRepository.findByUserIdAndIsPinnedTrueAndStatusNot(userId, NoteStatus.DELETED, pageable);
        return buildNoteListResponse(notesPage, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public UserNotesListResponseDTO getArchivedNotes(Long userId, Pageable pageable) {
        log.info("Fetching archived notes for user: {}", userId);

        Page<UserNotes> notesPage = userNotesRepository.findByUserIdAndStatus(userId, NoteStatus.ARCHIVED, pageable);
        return buildNoteListResponse(notesPage, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public UserNotesListResponseDTO searchNotes(Long userId, String searchTerm, Pageable pageable) {
        log.info("Searching notes for user: {} with term: {}", userId, searchTerm);

        Page<UserNotes> notesPage = userNotesRepository.searchNotes(userId, searchTerm, NoteStatus.DELETED, pageable);
        return buildNoteListResponse(notesPage, pageable);
    }

    @Override
    public UserNotesDTO updateNoteStatus(Long userId, Long noteId, NoteStatus status) {
        log.info("Updating note status {} for note {} and user {}", status, noteId, userId);

        UserNotes note = userNotesRepository.findByUserNoteIdAndUserId(noteId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Note not found with id: " + noteId));

        note.setStatus(status);
        note.setVersion(note.getVersion() + 1);
        UserNotes updatedNote = userNotesRepository.save(note);

        return convertToDTO(updatedNote);
    }

    @Override
    public UserNotesDTO togglePinStatus(Long userId, Long noteId) {
        log.info("Toggling pin status for note {} and user {}", noteId, userId);

        UserNotes note = userNotesRepository.findByUserNoteIdAndUserId(noteId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Note not found with id: " + noteId));

        note.setIsPinned(!note.getIsPinned());
        note.setVersion(note.getVersion() + 1);
        UserNotes updatedNote = userNotesRepository.save(note);

        return convertToDTO(updatedNote);
    }

    @Override
    public UserNotesDTO updateNoteColor(Long userId, Long noteId, NoteColor color) {
        log.info("Updating note color {} for note {} and user {}", color, noteId, userId);

        UserNotes note = userNotesRepository.findByUserNoteIdAndUserId(noteId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Note not found with id: " + noteId));

        note.setColor(color);
        note.setVersion(note.getVersion() + 1);
        UserNotes updatedNote = userNotesRepository.save(note);

        return convertToDTO(updatedNote);
    }

    @Override
    @Transactional(readOnly = true)
    public UserNotesStatsDTO getNoteStats(Long userId) {
        log.info("Fetching note statistics for user: {}", userId);

        Long totalNotes = userNotesRepository.countByUserIdAndStatusNot(userId, NoteStatus.DELETED);

        Map<String, Long> notesByType = convertToMap(userNotesRepository.countByNoteType(userId, NoteStatus.DELETED));
        Map<String, Long> notesByColor = convertToMap(userNotesRepository.countByColor(userId, NoteStatus.DELETED));
        Map<String, Long> notesByCategory = convertToMap(userNotesRepository.countByCategory(userId, NoteStatus.DELETED));
        Map<String, Long> notesByPriority = convertToMap(userNotesRepository.countByPriority(userId, NoteStatus.DELETED));
        Map<String, Long> notesByStatus = convertToMap(userNotesRepository.countByStatus(userId));

        Long pinnedNotes = userNotesRepository.countPinnedNotes(userId, NoteStatus.DELETED);
        Long sharedNotes = userNotesRepository.countSharedNotes(userId, NoteStatus.DELETED);
        Long notesWithReminders = userNotesRepository.countNotesWithReminders(userId, NoteStatus.DELETED);

        List<UserNotes> recentlyModifiedNotes = userNotesRepository.findRecentlyModified(userId, NoteStatus.DELETED, PageRequest.of(0, 5));
        List<UserNotesDTO> recentlyModified = recentlyModifiedNotes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return UserNotesStatsDTO.builder()
                .totalNotes(totalNotes)
                .notesByType(notesByType)
                .notesByColor(notesByColor)
                .notesByCategory(notesByCategory)
                .notesByPriority(notesByPriority)
                .notesByStatus(notesByStatus)
                .pinnedNotes(pinnedNotes)
                .sharedNotes(sharedNotes)
                .notesWithReminders(notesWithReminders)
                .recentlyModified(recentlyModified)
                .build();
    }

    @Override
    public List<UserNotesDTO> bulkUpdateNotes(Long userId, UserNotesBulkUpdateRequest request) {
        log.info("Bulk updating {} notes for user {}", request.getNoteIds().size(), userId);

        List<UserNotes> notes = userNotesRepository.findByUserNoteIdsAndUserId(request.getNoteIds(), userId);

        if (notes.size() != request.getNoteIds().size()) {
            throw new EntityNotFoundException("Some notes not found for the user");
        }

        notes.forEach(note -> {
            if (request.getColor() != null) note.setColor(request.getColor());
            if (request.getCategory() != null) note.setCategory(request.getCategory());
            if (request.getPriority() != null) note.setPriority(request.getPriority());
            if (request.getStatus() != null) note.setStatus(request.getStatus());
            if (request.getIsPinned() != null) note.setIsPinned(request.getIsPinned());
            note.setVersion(note.getVersion() + 1);
        });

        List<UserNotes> updatedNotes = userNotesRepository.saveAll(notes);

        return updatedNotes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void bulkDeleteNotes(Long userId, UserNotesBulkDeleteRequest request) {
        log.info("Bulk deleting {} notes for user {} (permanent: {})",
                request.getNoteIds().size(), userId, request.getPermanentDelete());

        List<UserNotes> notes = userNotesRepository.findByUserNoteIdsAndUserId(request.getNoteIds(), userId);

        if (notes.size() != request.getNoteIds().size()) {
            throw new EntityNotFoundException("Some notes not found for the user");
        }

        if (Boolean.TRUE.equals(request.getPermanentDelete())) {
            userNotesRepository.deleteAll(notes);
        } else {
            notes.forEach(note -> note.setStatus(NoteStatus.DELETED));
            userNotesRepository.saveAll(notes);
        }
    }

    @Override
    public UserNotesDTO duplicateNote(Long userId, Long noteId) {
        log.info("Duplicating note {} for user {}", noteId, userId);

        UserNotes originalNote = userNotesRepository.findByUserNoteIdAndUserId(noteId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Note not found with id: " + noteId));

        UserNotes duplicatedNote = UserNotes.builder()
                .userId(userId)
                .noteTitle(originalNote.getNoteTitle() + " (Copy)")
                .noteContent(originalNote.getNoteContent())
                .noteType(originalNote.getNoteType())
                .color(originalNote.getColor())
                .category(originalNote.getCategory())
                .priority(originalNote.getPriority())
                .status(NoteStatus.ACTIVE)
                .isPinned(false)
                .isShared(originalNote.getIsShared())
                .reminderDate(originalNote.getReminderDate())
                .version(1)
                .accessCount(0)
                .build();

        UserNotes savedNote = userNotesRepository.save(duplicatedNote);
        return convertToDTO(savedNote);
    }

    private UserNotesDTO convertToDTO(UserNotes note) {
        return UserNotesDTO.builder()
                .userNoteId(note.getUserNoteId())
                .userId(note.getUserId())
                .noteTitle(note.getNoteTitle())
                .noteContent(note.getNoteContent())
                .noteType(note.getNoteType())
                .color(note.getColor())
                .category(note.getCategory())
                .priority(note.getPriority())
                .status(note.getStatus())
                .isPinned(note.getIsPinned())
                .isShared(note.getIsShared())
                .reminderDate(note.getReminderDate())
                .version(note.getVersion())
                .accessCount(note.getAccessCount())
                .lastAccessedDate(note.getLastAccessedDate())
                .createdDate(note.getCreatedDate())
                .modifiedDate(note.getModifiedDate())
                .build();
    }

    private UserNotesListResponseDTO buildNoteListResponse(Page<UserNotes> notesPage, Pageable pageable) {
        List<UserNotesDTO> noteDTOs = notesPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        PaginationDTO pagination = PaginationDTO.builder()
                .currentPage(pageable.getPageNumber() + 1)
                .itemsPerPage(pageable.getPageSize())
                .totalItems(notesPage.getTotalElements())
                .totalPages(notesPage.getTotalPages())
                .hasPreviousPage(pageable.getPageNumber() > 0)
                .hasNextPage(pageable.getPageNumber() + 1 < notesPage.getTotalPages())
                .build();

        return UserNotesListResponseDTO.builder()
                .data(noteDTOs)
                .pagination(pagination)
                .build();
    }

    private Map<String, Long> convertToMap(List<Object[]> results) {
        return results.stream()
                .collect(Collectors.toMap(
                        result -> result[0].toString(),
                        result -> (Long) result[1]
                ));
    }
}
