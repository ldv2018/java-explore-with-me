package ru.practicum.ewmservice.mapper;

import lombok.NoArgsConstructor;
import ru.practicum.ewmservice.dto.*;
import ru.practicum.ewmservice.model.Event;
import ru.practicum.ewmservice.model.Location;
import ru.practicum.ewmservice.util.DateTimeUtils;

import java.time.LocalDateTime;

@NoArgsConstructor
public class EventMapper {
    public static Event toEvent(EventRequestDto eventRequestDto) {
        final LocalDateTime eventDate = DateTimeUtils.parse(eventRequestDto.getEventDate());

        return Event.builder()
                .annotation(eventRequestDto.getAnnotation())
                .description(eventRequestDto.getDescription())
                .eventDate(eventDate)
                .paid(eventRequestDto.isPaid())
                .participantLimit(eventRequestDto.getParticipantLimit())
                .requestModeration(eventRequestDto.isRequestModeration())
                .title(eventRequestDto.getTitle())
                .build();
    }

    public static EventResponseDto toEventResponseDto(Event event, Integer views) {
        EventResponseDto eventResponseDto = toEventResponseDto(event);
        eventResponseDto.setViews(views);

        return eventResponseDto;
    }

    public static EventResponseDto toEventResponseDto(Event event) {
        UserResponseDto initiator = UserResponseDto.builder()
                .id(event.getInitiator().getId())
                .name(event.getInitiator().getName())
                .build();
        LocationDto location = LocationDto.builder()
                .lon(event.getLocation().getLon())
                .lat(event.getLocation().getLat())
                .build();
        String publishedOn = null;
        if (event.getPublishedOn() != null) {
            publishedOn = DateTimeUtils.format(event.getPublishedOn());
        }

        return EventResponseDto.builder()
                .annotation(event.getAnnotation())
                .category(event.getCategory())
                .confirmedRequests(event.getConfirmedRequest())
                .createdOn(DateTimeUtils.format(event.getCreated()))
                .description(event.getDescription())
                .eventDate(DateTimeUtils.format(event.getEventDate()))
                .participantLimit(event.getParticipantLimit())
                .paid(event.getPaid())
                .id(event.getId())
                .title(event.getTitle())
                .state(event.getState())
                .initiator(initiator)
                .location(location)
                .requestModeration(event.getRequestModeration())
                .publishedOn(publishedOn)
                .build();
    }

    public static Event toEvent(final UpdateEventRequestDto eventDto) {
        LocalDateTime eventDate = null;
        if (eventDto.getEventDate() != null) {
            eventDate = DateTimeUtils.parse(eventDto.getEventDate());
        }
        Location location = null;
        if (eventDto.getLocation() != null) {
             location = LocationMapper.toLocation(eventDto.getLocation());
        }

        return Event.builder()
                .title(eventDto.getTitle())
                .description(eventDto.getDescription())
                .annotation(eventDto.getAnnotation())
                .eventDate(eventDate)
                .location(location)
                .participantLimit(eventDto.getParticipantLimit())
                .paid(eventDto.getPaid())
                .requestModeration(eventDto.getRequestModeration())
                .build();
    }
}