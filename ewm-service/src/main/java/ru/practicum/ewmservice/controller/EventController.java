package ru.practicum.ewmservice.controller;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewmservice.dto.EventRequestDto;
import ru.practicum.ewmservice.dto.EventResponseDto;
import ru.practicum.ewmservice.dto.UpdateEventRequestDto;
import ru.practicum.ewmservice.exception.BadRequestException;
import ru.practicum.ewmservice.mapper.EventMapper;
import ru.practicum.ewmservice.mapper.LocationMapper;
import ru.practicum.ewmservice.model.*;
import ru.practicum.ewmservice.service.CategoryService;
import ru.practicum.ewmservice.service.EventService;
import ru.practicum.ewmservice.service.LocationService;
import ru.practicum.ewmservice.service.UserService;
import ru.practicum.ewmservice.util.DateTimeUtils;
import ru.practicum.statsclient.StatsClient;
import ru.practicum.statsdto.HitDto;
import ru.practicum.statsdto.Stat;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ValidationException;
import javax.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@ComponentScan(basePackages = {"ru.practicum.statsclient"})
public class EventController {
    static final Set<UpdateEventStateAction> USER_ALLOWED_UPDATE_EVENT_STATE_ACTIONS
            = Set.of(UpdateEventStateAction.SEND_TO_REVIEW, UpdateEventStateAction.CANCEL_REVIEW);
    static final Set<UpdateEventStateAction> ADMIN_ALLOWED_UPDATE_EVENT_STATE_ACTIONS
            = Set.of(UpdateEventStateAction.PUBLISH_EVENT, UpdateEventStateAction.REJECT_EVENT);
    static final Set<String> SORTED_TYPE = Set.of("EVENT_DATE", "VIEWS");

    final EventService eventService;
    final UserService userService;
    final CategoryService categoryService;
    final LocationService locationService;
    final StatsClient statsClient;

//private path start
    @PostMapping("/users/{userId}/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventResponseDto add(@Validated @RequestBody EventRequestDto eventRequestDto,
                                @PathVariable int userId) {
        log.info("PRIVATE POST");
        log.info("Request from user id={} to add event:{}", userId, eventRequestDto);
        User initiator = userService.findById(userId);

        Location location = LocationMapper.toLocation(eventRequestDto.getLocation());
        Location savedLocation = locationService.add(location);
        Category category = categoryService.findById(eventRequestDto.getCategory());

        Event event = EventMapper.toEvent(eventRequestDto);
        event.setInitiator(initiator);
        event.setLocation(savedLocation);
        event.setCategory(category);

        Event savedEvent = eventService.add(event);

        return EventMapper.toEventResponseDto(savedEvent);
    }

    @GetMapping("/users/{userId}/events")
    @ResponseStatus(HttpStatus.OK)
    public List<EventResponseDto> get(
            @PathVariable int userId,
            @RequestParam(defaultValue = "0", required = false) @Min(0) int from,
            @RequestParam(defaultValue = "10", required = false) @Min(1) int size) {
        log.info("PRIVATE GET");
        log.info("Request from user id = {} to get events", userId);

        return eventService.getAllByUserId(userId, from, size).stream()
                .map(EventMapper::toEventResponseDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/users/{userId}/events/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventResponseDto get(@PathVariable int userId,
                                @PathVariable int eventId) {
        log.info("PRIVATE GET");
        log.info("Request from user id = {} to get event id = {}", userId, eventId);

        Event event = eventService.getByUserIdAndEventId(userId, eventId);
        return EventMapper.toEventResponseDto(event);
    }

    @PatchMapping("/users/{userId}/events/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventResponseDto update(@RequestBody @Validated UpdateEventRequestDto updateEventRequestDto,
                                   @PathVariable int userId,
                                   @PathVariable int eventId) {
        log.info("PRIVATE PATCH");
        log.info("Request from user id = {} to update event id = {}, event:{}",
                userId, eventId, updateEventRequestDto);

        Event.EventBuilder updateEventBuilder = EventMapper.toEvent(updateEventRequestDto).toBuilder();
        if (updateEventRequestDto.getStateAction() != null) {
            validateEventStateUpdateUserAction(updateEventRequestDto.getStateAction());
            State updateState =
                    updateEventRequestDto.getStateAction() == UpdateEventStateAction.SEND_TO_REVIEW
                            ? State.PENDING
                            : State.CANCELED;
            updateEventBuilder.state(updateState);
        }
        if (updateEventRequestDto.getCategory() != null) {
            Category category = categoryService.findById(updateEventRequestDto.getCategory());
            updateEventBuilder.category(category);
        }
        Event savedEvent = eventService.update(userId, eventId, updateEventBuilder.build());

        return EventMapper.toEventResponseDto(savedEvent);
    }
//private path end

//admin path start
    @GetMapping("/admin/events")
    @ResponseStatus(HttpStatus.OK)
    public List<EventResponseDto> getAll(@RequestParam(required = false) List<Integer> users,
                                         @RequestParam(required = false) List<String> states,
                                         @RequestParam(required = false) List<Integer> categories,
                                         @RequestParam(required = false) String rangeStart,
                                         @RequestParam(required = false) String rangeEnd,
                                         @RequestParam(defaultValue = "0", required = false) @Min(0) int from,
                                         @RequestParam(defaultValue = "10", required = false) @Min(1) int size) {
        log.info("ADMIN GET");
        log.info("Admin request for events. " +
                "\nusers = {}; " +
                "\nstates = {}; " +
                "\ncategories = {};" +
                "\nrangeStart = {}; " +
                "\nrangeEnd = {}; " +
                "\nfrom = {}, size = {}.", users, states, categories, rangeStart, rangeEnd, from, size);
        List<State> eventStates = Optional.ofNullable(states)
                .map(list -> list.stream()
                        .filter(Objects::nonNull)
                        .map(State::valueOf)
                        .collect(Collectors.toList()))
                .orElse(null);
        LocalDateTime eventStart = Optional.ofNullable(rangeStart)
                .map(DateTimeUtils::parse)
                .orElse(null);
        LocalDateTime eventEnd = Optional.ofNullable(rangeEnd)
                .map(DateTimeUtils::parse)
                .orElse(null);

        return eventService.getAllByUsersAndStatesAndCategoriesAndStartAndEnd(users,
                eventStates,
                categories,
                eventStart,
                eventEnd,
                from,
                size).stream()
                     .map(EventMapper::toEventResponseDto)
                     .collect(Collectors.toList());
    }

    @PatchMapping("/admin/events/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventResponseDto update(@RequestBody UpdateEventRequestDto updateEventRequestDto,
                                   @PathVariable int eventId) {
        log.info("ADMIN PATCH");
        log.info("Admin request for update event id = {}; " +
                "\nupdated event: {}", eventId, updateEventRequestDto);
        Event.EventBuilder updateEventBuilder = EventMapper.toEvent(updateEventRequestDto).toBuilder();
        if (updateEventRequestDto.getStateAction() != null) {
            validateEventStateUpdateAdminAction(updateEventRequestDto.getStateAction());
            State updateState =
                    updateEventRequestDto.getStateAction() == UpdateEventStateAction.PUBLISH_EVENT
                            ? State.PUBLISHED
                            : State.CANCELED;
            updateEventBuilder.state(updateState);
        }
        if (updateEventRequestDto.getCategory() != null) {
            Category category = categoryService.findById(updateEventRequestDto.getCategory());
            updateEventBuilder.category(category);
        }
        Event savedEvent = eventService.update(eventId, updateEventBuilder.build());

        return EventMapper.toEventResponseDto(savedEvent);
    }
//admin path end

//common path start
    @GetMapping("/events")
    @ResponseStatus(HttpStatus.OK)
    public List<EventResponseDto> getAll(@RequestParam(required = false) String text,
                                         @RequestParam(required = false) List<Integer> categories,
                                         @RequestParam(required = false) Boolean paid,
                                         @RequestParam(required = false) String rangeStart,
                                         @RequestParam(required = false) String rangeEnd,
                                         @RequestParam(required = false) Boolean onlyAvailable,
                                         @RequestParam(required = false) String sort,
                                         @RequestParam(defaultValue = "0", required = false) @Min(0) int from,
                                         @RequestParam(defaultValue = "10", required = false) @Min(1) int size,
                                         HttpServletRequest request) {
        log.info("COMMON GET");
        log.info("Common request for events. " +
                "\ntext = {}; " +
                "\ncategories = {};" +
                "\npaid = {} ;" +
                "\nrangeStart = {}; " +
                "\nrangeEnd = {}; " +
                "\nfrom = {}, size = {}.", text, categories, paid, rangeStart, rangeEnd, from, size);
        LocalDateTime eventStart = Optional.ofNullable(rangeStart)
                .map(DateTimeUtils::parse)
                .orElse(null);
        LocalDateTime eventEnd = Optional.ofNullable(rangeEnd)
                .map(DateTimeUtils::parse)
                .orElse(null);
        if (eventStart == null && eventEnd == null) {
            eventStart = LocalDateTime.now();
        }
        if (sort != null) {
            if (!SORTED_TYPE.contains(sort)) {
                throw new BadRequestException(HttpStatus.BAD_REQUEST, String.format("Invalid value for 'sort': %s", sort));
            }
        }

        HitDto hitDto = new HitDto("EwmServiceApplication",
                request.getRequestURI(),
                request.getRemoteAddr(),
                DateTimeUtils.format(LocalDateTime.now()));
        statsClient.saveStat(hitDto); //это сохранится по /events, м.б. надо будет исправить на весь list
        List<Event> events = new ArrayList<>(eventService.searchPublishedEvents(
                text, categories, paid, eventStart, eventEnd, onlyAvailable, from, size));
        List<String> uris = events.stream()
                .map(e -> String.format("events/%d", e.getId()))
                .collect(Collectors.toList());
        List<Stat> stats = statsClient.getStat(null, null, uris, false);
        Map<String, Integer> s = new HashMap<>();
        for (Stat stat : stats) {
            s.put(stat.getUri(), stat.getHits());
        }
        List<EventResponseDto> eventsDto = events.stream()
                .map(e -> EventMapper.toEventResponseDto(e, s.get(String.format("events/%d", e.getId()))))
                .collect(Collectors.toList());
        if (sort != null) {
            if (sort.equals("VIEWS")) {
                eventsDto = eventsDto.stream()
                        .sorted(Comparator.comparing(EventResponseDto::getViews))
                        .collect(Collectors.toList());
            }
        }

        return eventsDto;
    }

    @GetMapping("/events/{id}")
    @ResponseStatus(HttpStatus.OK)
    public EventResponseDto get(@PathVariable int id,
                                HttpServletRequest request) {
        log.info("COMMON GET");
        log.info("Public request for event with id = {}", id);
        Event event = eventService.getPublishedEventById(id);
        List<Stat> stats = statsClient.getStat(null,
                null,
                List.of(request.getRequestURI()),
                false);
        HitDto hitDto = new HitDto("EwmServiceApplication",
                request.getRequestURI(),
                request.getRemoteAddr(),
                DateTimeUtils.format(LocalDateTime.now()));
        statsClient.saveStat(hitDto);

        return EventMapper.toEventResponseDto(event, stats.get(0).getHits());
    }
//common path end

    private void validateEventStateUpdateUserAction(
            @NonNull final UpdateEventStateAction stateAction
    ) throws ValidationException {
        if (!USER_ALLOWED_UPDATE_EVENT_STATE_ACTIONS.contains(stateAction)) {
            throw new ValidationException("Not allowed state update for User");
        }
    }

    private void validateEventStateUpdateAdminAction(
            @NonNull final UpdateEventStateAction stateAction
    ) throws ValidationException {
        if (!ADMIN_ALLOWED_UPDATE_EVENT_STATE_ACTIONS.contains(stateAction)) {
            throw new ValidationException("Not allowed state update for Admin");
        }
    }
}
