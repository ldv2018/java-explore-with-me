package ru.practicum.ewmservice.storage;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.ewmservice.model.Comment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {

    Optional<Comment> findByUserIdAndEventId(int userId, int eventId);

    Page<Comment> findAllByUserId(int userId, Pageable pageable);

    @Query("SELECT c FROM Comment AS c " +
            "JOIN c.user AS u " +
            "JOIN c.event AS e " +
            "WHERE (:users IS NULL OR u.id IN :users) " +
            "AND (:events IS NULL OR e.id IN :events) " +
            "AND (:approved IS NULL OR c.isApproved IN :approved) " +
            "AND (CAST(:start AS timestamp) IS NULL OR c.publishedOn >= :start) " +
            "AND (CAST(:end AS timestamp) IS NULL OR c.publishedOn <= :end)")
    Page<Comment> getAllByUsersAndEventsAndApprovedAndPublishedOn(
            @Param("users") List<Integer> userIds,
            @Param("events") List<Integer> eventIds,
            @Param("approved") boolean isApproved,
            @Param("start") LocalDateTime commentCreatedStart,
            @Param("end") LocalDateTime commentCreatedEnd,
            Pageable pageable);

    @Query("SELECT c FROM Comment AS c " +
            "JOIN c.event AS e " +
            "WHERE e.id = :event")
    Page<Comment> getALlByEventIdAndApproved(@Param("event") int eventId, Pageable pageable);
}
