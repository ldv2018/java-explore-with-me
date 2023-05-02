package ru.practicum.ewmservice.storage;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.ewmservice.model.Event;
import ru.practicum.ewmservice.model.State;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Integer> {
    @Query("SELECT e " +
            "FROM Event AS e " +
            "JOIN e.initiator AS u " +
            "JOIN e.category AS c " +
            "WHERE u.id = ?1")
    Page<Event> findAllByInitiatorId(int userId, Pageable pageable);

    Optional<Event> findByIdAndInitiatorId(int eventId, int userId);

    @Query("SELECT e FROM Event AS e " +
            "JOIN e.initiator AS u " +
            "JOIN e.category AS c " +
            "WHERE (:users IS NULL OR u.id IN :users) " +
            "AND (:eventStates IS NULL OR e.state IN :eventStates)" +
            "AND (:categories IS NULL OR c.id IN :categories) " +
            "AND (CAST(:eventStart AS timestamp) IS NULL OR e.eventDate >= :eventStart) " +
            "AND (CAST(:eventEnd AS timestamp) IS NULL OR e.eventDate <= :eventEnd)")
    Page<Event> findAllByInitiatorIdAndStateAndCategoriesAndEventDate(
            @Param("users") List<Integer> users,
            @Param("eventStates") List<State> eventStates,
            @Param("categories") List<Integer> categories,
            @Param("eventStart") LocalDateTime eventStart,
            @Param("eventEnd") LocalDateTime eventEnd,
            Pageable pageable
            );

    @Query("SELECT e " +
            "FROM Event AS e " +
            "JOIN e.initiator AS u " +
            "JOIN e.category AS c " +
            "WHERE e.state = 'PUBLISHED' " +
            "  AND (:text IS NULL " +
            "OR (" +
            "UPPER(e.description) LIKE UPPER(CONCAT('%', :text, '%')) " +
            "OR UPPER(e.annotation) LIKE UPPER(CONCAT('%', :text, '%')))) " +
            "AND (:paid IS NULL OR e.paid = :paid) " +
            "AND (:onlyAvailable IS FALSE OR e.confirmedRequest < e.participantLimit) " +
            "AND (:categories IS NULL OR c.id IN :categories) " +
            "AND (CAST(:rangeStart AS timestamp) IS NULL OR e.eventDate >= :rangeStart) " +
            "AND (CAST(:rangeEnd AS timestamp) IS NULL OR e.eventDate <= :rangeEnd) " +
            "ORDER BY e.eventDate ASC")
    Page<Event> searchPublishedEventsOrderByEventDateAsc(
            @Param("text") String text,
            @Param("categories") List<Integer> categories,
            @Param("paid") Boolean paid,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            @Param("onlyAvailable") Boolean onlyAvailable,
            Pageable pageable
    );

    Optional<Event> findByIdAndStateEquals(int id, State state);
}