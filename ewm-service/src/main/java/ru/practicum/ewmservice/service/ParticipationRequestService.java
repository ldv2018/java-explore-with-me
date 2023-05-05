package ru.practicum.ewmservice.service;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.practicum.ewmservice.model.Event;
import ru.practicum.ewmservice.model.ParticipationRequest;
import ru.practicum.ewmservice.model.ParticipationRequestState;
import ru.practicum.ewmservice.model.State;
import ru.practicum.ewmservice.exception.ForbiddenOperation;
import ru.practicum.ewmservice.exception.NotFoundException;
import ru.practicum.ewmservice.storage.EventRepository;
import ru.practicum.ewmservice.storage.ParticipationRequestRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class ParticipationRequestService {

    static final String NOT_FOUND_MSG_FORMAT = "Event request with id=%d was not found";
    static final String USER_REQUEST_FOR_UNPUBLISHED_EVENT_IS_REJECTED_ERROR_MSG = "Event must be published to participate";
    final ParticipationRequestRepository participationRequestRepository;
    final EventRepository eventRepository;

    public ParticipationRequest create(final @NonNull ParticipationRequest request) {
        Event event = request.getEvent();

        if (!State.PUBLISHED.equals(event.getState())) {
            throw new ForbiddenOperation(HttpStatus.FORBIDDEN,
                    USER_REQUEST_FOR_UNPUBLISHED_EVENT_IS_REJECTED_ERROR_MSG);
        } else if (event.getParticipantLimit()
                .equals(event.getConfirmedRequest())) {
            throw new ForbiddenOperation(HttpStatus.FORBIDDEN,
                    "Event must have free slots to participate");
        }
        if (request.getEvent().getInitiator().getId() == (request.getRequester().getId())) {
            throw new ForbiddenOperation(HttpStatus.FORBIDDEN,
                    "Event initiator couldn't create request to participate in its own event");
        }
        boolean isAutoApproval = !event.getRequestModeration();
        ParticipationRequest.ParticipationRequestBuilder participationRequestBuilder = request.toBuilder();

        if (isAutoApproval) {
            participationRequestBuilder.status(ParticipationRequestState.CONFIRMED);
        } else {
            participationRequestBuilder.status(ParticipationRequestState.PENDING);
        }
        participationRequestBuilder.createdOn(LocalDateTime.now());

        return participationRequestRepository.save(participationRequestBuilder.build());
    }

    public List<ParticipationRequest> getAllByRequester(int requesterId) {
        return participationRequestRepository.findAllByRequesterId(requesterId);
    }

    public List<ParticipationRequest> getAllForEventIdByInitiator(List<Integer> requestIds,
                                                                  int eventId,
                                                                  int initiatorId) {
        return participationRequestRepository
                .findAllWhereRequestIdInAndEventIdEqualsAndInitiatorIdEqualsAndStatusEquals(requestIds,
                        eventId,
                        initiatorId,
                        null
        );
    }

    public ParticipationRequest getById(int requestId) throws NotFoundException {
        ParticipationRequest participationRequest = participationRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(HttpStatus.NOT_FOUND,
                        "Participation request with id=" + requestId + " was not found"));

        return participationRequest;
    }

    public ParticipationRequest cancelParticipationRequest(int requestId, int userId) throws NotFoundException {
        ParticipationRequest participationRequest = this.getById(requestId);
        if (participationRequest.getRequester().getId() != userId) {
            String errorMessage = String.format(NOT_FOUND_MSG_FORMAT, requestId);
            log.error(errorMessage);
            throw new NotFoundException(HttpStatus.NOT_FOUND, errorMessage);
        }

        ParticipationRequest.ParticipationRequestBuilder updatedEventRequestBuilder = participationRequest.toBuilder();
        updatedEventRequestBuilder.status(ParticipationRequestState.CANCELED);
        return participationRequestRepository.save(updatedEventRequestBuilder.build());
    }

    public List<ParticipationRequest> confirmParticipationRequest(
            @NonNull List<Integer> requestIds,
            int eventId,
            int initiatorId
    ) throws NotFoundException, ForbiddenOperation {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(HttpStatus.NOT_FOUND,
                        String.format("Event with id=%d was not found", eventId)));

        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            return this.getAllForEventIdByInitiator(
                    requestIds, eventId, initiatorId
            );
        }

        final List<ParticipationRequest> participationRequests = this.getAllForEventIdByInitiator(requestIds, eventId, initiatorId);
        final List<ParticipationRequest> confirmedEventRequests = new ArrayList<>();

        for (ParticipationRequest participationRequest : participationRequests) {
            if (!ParticipationRequestState.PENDING.equals(participationRequest.getStatus())) {
                throw new ForbiddenOperation(HttpStatus.FORBIDDEN,
                        "Request should be in non-terminal state");
            }

            ParticipationRequest updatedParticipationRequest = participationRequest.toBuilder()
                    .status(ParticipationRequestState.CONFIRMED)
                    .build();

            confirmedEventRequests.add(updatedParticipationRequest);
        }
        eventRepository.save(event);
        participationRequestRepository.saveAll(confirmedEventRequests);
        if (!Objects.equals(event.getConfirmedRequest(), event.getParticipantLimit())) {
            return confirmedEventRequests;
        }
        List<ParticipationRequest> rejectedParticipationRequest = new ArrayList<>();
        List<ParticipationRequest> pendingParticipationRequest = participationRequestRepository
                .findAllWhereRequestIdInAndEventIdEqualsAndInitiatorIdEqualsAndStatusEquals(
                        null, eventId, initiatorId, ParticipationRequestState.PENDING
                );

        for (ParticipationRequest participationRequest : pendingParticipationRequest) {
            ParticipationRequest updatedParticipationRequest = participationRequest.toBuilder()
                    .status(ParticipationRequestState.REJECTED)
                    .build();
            rejectedParticipationRequest.add(updatedParticipationRequest);
        }
        participationRequestRepository.saveAll(rejectedParticipationRequest);

        return confirmedEventRequests;
    }

    public List<ParticipationRequest> rejectParticipationRequest(
            @NonNull List<Integer> requestIds,
            int eventId,
            int initiatorId
    ) throws NotFoundException, ForbiddenOperation {
        List<ParticipationRequest> participationRequest =
                this.getAllForEventIdByInitiator(requestIds, eventId, initiatorId);
        List<ParticipationRequest> rejectedParticipationRequest = new ArrayList<>();

        for (ParticipationRequest pr : participationRequest) {
            if (!ParticipationRequestState.PENDING.equals(pr.getStatus())) {
                throw new ForbiddenOperation(HttpStatus.FORBIDDEN,
                        "Request should be in non-terminal state");
            }
            ParticipationRequest updatedEParticipationRequest = pr.toBuilder()
                    .status(ParticipationRequestState.REJECTED)
                    .build();

            rejectedParticipationRequest.add(updatedEParticipationRequest);
        }
        participationRequestRepository.saveAll(rejectedParticipationRequest);

        return rejectedParticipationRequest;
    }
}
