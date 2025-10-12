package com.sid.app.repository;

import com.sid.app.entity.UserNotes;
import com.sid.app.enums.NoteCategory;
import com.sid.app.enums.NoteStatus;
import com.sid.app.enums.NoteType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserNotesRepository extends JpaRepository<UserNotes, Long> {

    // Find notes by user ID with pagination
    Page<UserNotes> findByUserIdAndStatusNot(Long userId, NoteStatus status, Pageable pageable);

    // Find specific note by ID and user ID
    Optional<UserNotes> findByUserNoteIdAndUserId(Long userNoteId, Long userId);

    // Find notes by user ID and type
    Page<UserNotes> findByUserIdAndNoteTypeAndStatusNot(Long userId, NoteType noteType, NoteStatus status, Pageable pageable);

    // Find notes by user ID and category
    Page<UserNotes> findByUserIdAndCategoryAndStatusNot(Long userId, NoteCategory category, NoteStatus status, Pageable pageable);

    // Find pinned notes
    Page<UserNotes> findByUserIdAndIsPinnedTrueAndStatusNot(Long userId, NoteStatus status, Pageable pageable);

    // Find archived notes
    Page<UserNotes> findByUserIdAndStatus(Long userId, NoteStatus status, Pageable pageable);

    // Search notes by title and content
    @Query("SELECT n FROM UserNotes n WHERE n.userId = :userId AND n.status != :excludeStatus AND " +
           "(LOWER(n.noteTitle) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(n.noteContent) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<UserNotes> searchNotes(@Param("userId") Long userId,
                          @Param("searchTerm") String searchTerm,
                          @Param("excludeStatus") NoteStatus excludeStatus,
                          Pageable pageable);

    // Complex filtering query
    @Query("SELECT n FROM UserNotes n WHERE n.userId = :userId " +
           "AND (:noteType IS NULL OR n.noteType = :noteType) " +
           "AND (:color IS NULL OR n.color = :color) " +
           "AND (:category IS NULL OR n.category = :category) " +
           "AND (:priority IS NULL OR n.priority = :priority) " +
           "AND (:status IS NULL OR n.status = :status) " +
           "AND (:isPinned IS NULL OR n.isPinned = :isPinned) " +
           "AND (:isShared IS NULL OR n.isShared = :isShared) " +
           "AND (:startDate IS NULL OR n.createdDate >= :startDate) " +
           "AND (:endDate IS NULL OR n.createdDate <= :endDate)")
    Page<UserNotes> findNotesWithFilters(@Param("userId") Long userId,
                                   @Param("noteType") NoteType noteType,
                                   @Param("color") com.sid.app.enums.NoteColor color,
                                   @Param("category") NoteCategory category,
                                   @Param("priority") com.sid.app.enums.NotePriority priority,
                                   @Param("status") NoteStatus status,
                                   @Param("isPinned") Boolean isPinned,
                                   @Param("isShared") Boolean isShared,
                                   @Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate,
                                   Pageable pageable);

    // Statistics queries
    @Query("SELECT COUNT(n) FROM UserNotes n WHERE n.userId = :userId AND n.status != :excludeStatus")
    Long countByUserIdAndStatusNot(@Param("userId") Long userId, @Param("excludeStatus") NoteStatus excludeStatus);

    @Query("SELECT n.noteType, COUNT(n) FROM UserNotes n WHERE n.userId = :userId AND n.status != :excludeStatus GROUP BY n.noteType")
    List<Object[]> countByNoteType(@Param("userId") Long userId, @Param("excludeStatus") NoteStatus excludeStatus);

    @Query("SELECT n.color, COUNT(n) FROM UserNotes n WHERE n.userId = :userId AND n.status != :excludeStatus GROUP BY n.color")
    List<Object[]> countByColor(@Param("userId") Long userId, @Param("excludeStatus") NoteStatus excludeStatus);

    @Query("SELECT n.category, COUNT(n) FROM UserNotes n WHERE n.userId = :userId AND n.status != :excludeStatus GROUP BY n.category")
    List<Object[]> countByCategory(@Param("userId") Long userId, @Param("excludeStatus") NoteStatus excludeStatus);

    @Query("SELECT n.priority, COUNT(n) FROM UserNotes n WHERE n.userId = :userId AND n.status != :excludeStatus GROUP BY n.priority")
    List<Object[]> countByPriority(@Param("userId") Long userId, @Param("excludeStatus") NoteStatus excludeStatus);

    @Query("SELECT n.status, COUNT(n) FROM UserNotes n WHERE n.userId = :userId GROUP BY n.status")
    List<Object[]> countByStatus(@Param("userId") Long userId);

    @Query("SELECT COUNT(n) FROM UserNotes n WHERE n.userId = :userId AND n.isPinned = true AND n.status != :excludeStatus")
    Long countPinnedNotes(@Param("userId") Long userId, @Param("excludeStatus") NoteStatus excludeStatus);

    @Query("SELECT COUNT(n) FROM UserNotes n WHERE n.userId = :userId AND n.isShared = true AND n.status != :excludeStatus")
    Long countSharedNotes(@Param("userId") Long userId, @Param("excludeStatus") NoteStatus excludeStatus);

    @Query("SELECT COUNT(n) FROM UserNotes n WHERE n.userId = :userId AND n.reminderDate IS NOT NULL AND n.status != :excludeStatus")
    Long countNotesWithReminders(@Param("userId") Long userId, @Param("excludeStatus") NoteStatus excludeStatus);

    // Recently modified notes
    @Query("SELECT n FROM UserNotes n WHERE n.userId = :userId AND n.status != :excludeStatus ORDER BY n.modifiedDate DESC")
    List<UserNotes> findRecentlyModified(@Param("userId") Long userId, @Param("excludeStatus") NoteStatus excludeStatus, Pageable pageable);

    // Bulk operations
    @Query("SELECT n FROM UserNotes n WHERE n.userNoteId IN :userNoteIds AND n.userId = :userId")
    List<UserNotes> findByUserNoteIdsAndUserId(@Param("userNoteIds") List<Long> userNoteIds, @Param("userId") Long userId);

    // Update access count and last accessed date
    @Modifying
    @Query("UPDATE UserNotes n SET n.accessCount = n.accessCount + 1, n.lastAccessedDate = :accessTime WHERE n.userNoteId = :userNoteId AND n.userId = :userId")
    void updateAccessInfo(@Param("userNoteId") Long userNoteId, @Param("userId") Long userId, @Param("accessTime") LocalDateTime accessTime);
}
