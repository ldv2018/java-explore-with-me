package ru.practicum.ewmservice.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.practicum.ewmservice.exception.ForbiddenOperation;
import ru.practicum.ewmservice.exception.NotFoundException;
import ru.practicum.ewmservice.model.Event;
import ru.practicum.ewmservice.model.State;
import ru.practicum.ewmservice.storage.EventRepository;
import ru.practicum.ewmservice.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventService {
    static final String UPDATE_PUBLISHED_EVENT = "Only pending or canceled events can be changed by user";
    static final String ADMIN_PUBLISH_EVENT_IS_REJECTED_ERROR = "Cannot publish the event because it's not in the right state: %s";
    static final String ADMIN_CANCEL_EVENT_IS_REJECTED_ERROR = "Cannot cancel the event because it's not in the right state: %s";
    static final String INVALID_EVENT_DATE_ERROR = "eventDate должно содержать дату после: %s";
    static final int MINIMAL_EVENT_DATE_HOURS = 2;
    final EventRepository eventRepository;
    final UserRepository userRepository;

    public Event get(int id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(HttpStatus.NOT_FOUND,
                        String.format("Event with id=%d not found", id)));
    }

    public Event add(Event event) {
        throwIfEventDateNotValid(event.getEventDate());
        event.setCreated(LocalDateTime.now());
        event.setState(State.PENDING);

        return eventRepository.save(event);
    }

    public List<Event> getAllByUserId(int userId, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);

        return eventRepository.findAllByInitiatorId(userId, pageable)
                .getContent();
    }

    public Event getByUserIdAndEventId(int userId, int eventId) {
        throwIfUserIdNotValid(userId);

        return eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException(HttpStatus.NOT_FOUND,
                        "event id " + eventId + " not found"));
    }

    public Event update(int userId, int eventId, Event updateEvent) {
        throwIfUserIdNotValid(userId);
        Event event = getByUserIdAndEventId(userId, eventId);
        if (updateEvent.getEventDate() != null) {
            throwIfEventDateNotValid(updateEvent.getEventDate());
        }
        if (event.getState() == State.PUBLISHED) {
            throw new ForbiddenOperation(HttpStatus.FORBIDDEN, UPDATE_PUBLISHED_EVENT);
        }
        Event updatedEvent = this.updateEvent(event, updateEvent).build();

        return eventRepository.save(updatedEvent);
    }

    private Event.EventBuilder updateEvent(Event event, Event updateEvent) {
        Event.EventBuilder updatedEventBuilder = event.toBuilder();

        if (updateEvent.getTitle() != null
                && !event.getTitle().equals(updateEvent.getTitle())) {
            updatedEventBuilder.title(updateEvent.getTitle());
        }
        if (updateEvent.getDescription() != null
                && !event.getDescription().equals(updateEvent.getDescription())) {
            updatedEventBuilder.description(updateEvent.getDescription());
        }
        if (updateEvent.getAnnotation() != null
                && !event.getAnnotation().equals(updateEvent.getAnnotation())) {
            updatedEventBuilder.annotation(updateEvent.getAnnotation());
        }
        if (updateEvent.getEventDate() != null
                && !event.getEventDate().equals(updateEvent.getEventDate())) {
            updatedEventBuilder.eventDate(updateEvent.getEventDate());
        }
        if (updateEvent.getLocation() != null
                && !event.getLocation().equals(updateEvent.getLocation())) {
            updatedEventBuilder.location(updateEvent.getLocation());
        }
        if (updateEvent.getParticipantLimit() != null
                && !event.getParticipantLimit().equals(updateEvent.getParticipantLimit())) {
            updatedEventBuilder.participantLimit(updateEvent.getParticipantLimit());
        }
        if (updateEvent.getPaid() != null
                && !event.getPaid().equals(updateEvent.getPaid())) {
            updatedEventBuilder.paid(updateEvent.getPaid());
        }
        if (updateEvent.getRequestModeration() != null
                && !event.getRequestModeration().equals(updateEvent.getRequestModeration())) {
            updatedEventBuilder.requestModeration(updateEvent.getRequestModeration());
        }
        if (updateEvent.getCategory() != null
                && !event.getCategory().equals(updateEvent.getCategory())) {
            updatedEventBuilder.category(updateEvent.getCategory());
        }
        if (updateEvent.getState() != null
                && !event.getState().equals(updateEvent.getState())) {
            updatedEventBuilder.state(updateEvent.getState());
        }

        return updatedEventBuilder;
    }

//admin
    public List<Event> getAllByUsersAndStatesAnsCategoriesAndStartAndEnd(List<Integer> users,
                                                                         List<State> eventStates,
                                                                         List<Integer> categories,
                                                                         LocalDateTime eventStart,
                                                                         LocalDateTime eventEnd,
                                                                         int from,
                                                                         int size) {
        Pageable pageable = PageRequest.of(from / size, size);

        return eventRepository.findAllByInitiatorIdAndStateAndCategoriesAndEventDate(
                users,
                eventStates,
                categories,
                eventStart,
                eventEnd,
                pageable
        ).getContent();
    }

    public Event update(int eventId, Event updateEvent) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(HttpStatus.NOT_FOUND, "event id = " + eventId + " not found"));
        Event.EventBuilder eventBuilder = event.toBuilder();

        if (updateEvent.getEventDate() != null) {
            throwIfEventDateNotValid(updateEvent.getEventDate());
        }
        if (updateEvent.getState().equals(State.PUBLISHED)) {
            if (!event.getState().equals(State.PENDING)) {
                throw new ForbiddenOperation(HttpStatus.FORBIDDEN,
                        String.format(ADMIN_PUBLISH_EVENT_IS_REJECTED_ERROR, event.getState()));
            }
            LocalDateTime eventDate = Optional.ofNullable(updateEvent.getEventDate())
                    .orElse(event.getEventDate());
            LocalDateTime minEventDate = LocalDateTime.now().plusHours(1);
            if (eventDate.isBefore(minEventDate)) {
                throw new ForbiddenOperation(HttpStatus.FORBIDDEN,
                        String.format(INVALID_EVENT_DATE_ERROR, minEventDate));
            }
            eventBuilder.publishedOn(LocalDateTime.now());
        } else if (event.getState().equals(State.PUBLISHED) &&
                updateEvent.getState().equals(State.CANCELED)) {
            throw new ForbiddenOperation(HttpStatus.FORBIDDEN,
                    String.format(ADMIN_CANCEL_EVENT_IS_REJECTED_ERROR, event.getState()));
        }
        Event updatedEvent = this.updateEvent(event, updateEvent).build();

        return eventRepository.save(updatedEvent);
    }

//common
    public List<Event> searchPublishedEvents(String text,
                                             List<Integer> categories,
                                             Boolean paid,
                                             LocalDateTime eventStart,
                                             LocalDateTime eventEnd,
                                             Boolean onlyAvailable,
                                             int from,
                                             int size) {
        Pageable pageable = PageRequest.of(from / size, size);

        return eventRepository.searchPublishedEventsOrderByEventDateAsc(text,
                categories,
                paid,
                eventStart,
                eventEnd,
                onlyAvailable,
                pageable).getContent();
    }

    public Event getPublishedEventById(int id) {
        return eventRepository.findByIdAndStateEquals(id, State.PUBLISHED)
                .orElseThrow(() -> new NotFoundException(HttpStatus.NOT_FOUND,
                        "event id " + id + " not found"));
    }

//for compilation
    public List<Event> getAllByIds(List<Integer> eventIds) {
        return eventRepository.findAllById(eventIds);
}

    private void throwIfEventDateNotValid(LocalDateTime eventDate) {
        LocalDateTime minimalEventDate = LocalDateTime.now().plusHours(MINIMAL_EVENT_DATE_HOURS);
        if (eventDate.isBefore(minimalEventDate)) {
            throw new ForbiddenOperation(HttpStatus.FORBIDDEN,
                    String.format(INVALID_EVENT_DATE_ERROR, minimalEventDate));
        }
    }

    private void throwIfUserIdNotValid(int userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(HttpStatus.NOT_FOUND,
                    "user id " + userId + " not found");
        }
    }
}
