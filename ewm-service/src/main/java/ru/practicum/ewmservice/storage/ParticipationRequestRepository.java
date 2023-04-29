package ru.practicum.ewmservice.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.ewmservice.model.ParticipationRequest;
import ru.practicum.ewmservice.model.ParticipationRequestState;

import java.util.List;

@Repository
public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Integer> {
    List<ParticipationRequest> findAllByRequesterId(int requesterId);

    @Query("SELECT r " +
            "FROM ParticipationRequest AS r " +
            "JOIN r.event AS e " +
            "JOIN e.initiator AS u " +
            "WHERE (:requestIds IS NULL OR r.id IN :requestIds) " +
            "  AND e.id = :eventId " +
            "  AND u.id = :initiatorId " +
            "  AND (:status IS NULL OR r.status = :status) "
    )
    List<ParticipationRequest> findAllWhereRequestIdInAndEventIdEqualsAndInitiatorIdEqualsAndStatusEquals(
            @Param("requestIds") List<Integer> requestIds,
            @Param("eventId") int eventId,
            @Param("initiatorId") int initiatorId,
            @Param("status") ParticipationRequestState status
    );
}
