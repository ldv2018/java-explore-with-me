package ru.practicum.ewmservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewmservice.dto.CommentRequestDto;
import ru.practicum.ewmservice.dto.CommentResponseDto;
import ru.practicum.ewmservice.exception.ForbiddenOperation;
import ru.practicum.ewmservice.mapper.CommentMapper;
import ru.practicum.ewmservice.model.Comment;
import ru.practicum.ewmservice.model.Event;
import ru.practicum.ewmservice.model.User;
import ru.practicum.ewmservice.service.CommentService;
import ru.practicum.ewmservice.service.EventService;
import ru.practicum.ewmservice.service.UserService;
import ru.practicum.ewmservice.util.DateTimeUtils;

import javax.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Slf4j
public class CommentController {

    final CommentService commentService;
    final EventService eventService;
    final UserService userService;

//private path
    @PostMapping("/users/{userId}/comments/{eventId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponseDto add(@Validated @RequestBody CommentRequestDto commentRequestDto,
                                  @PathVariable int userId,
                                  @PathVariable int eventId) {
        log.info("PRIVATE POST");
        log.info("Request for add Comment: {}; userId = {}; eventId = {}", commentRequestDto, userId, eventId);
        User user = userService.findById(userId);
        Event event = eventService.get(eventId);
        if (event.getInitiator().getId() == userId) {
            throw new ForbiddenOperation(HttpStatus.FORBIDDEN, "Owner couldn't add comment to own event");
        }
        Comment comment = CommentMapper.toComment(commentRequestDto);
        comment.setUser(user);
        comment.setEvent(event);
        Comment savedComment = commentService.add(comment);

        return CommentMapper.toCommentResponseDto(savedComment);
    }

    @PatchMapping("/users/{userId}/comments/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public CommentResponseDto update(@Validated @RequestBody CommentRequestDto commentRequestDto,
                                     @PathVariable int userId,
                                     @PathVariable int eventId) {
        log.info("PRIVATE UPDATE");
        log.info("Request for update Comment: {}; userId = {}; eventId = {}", commentRequestDto, userId, eventId);
        Comment comment = commentService.getByEventId(userId, eventId);
        comment.setContent(commentRequestDto.getContent());
        Comment updatedComment = commentService.update(comment);

        return CommentMapper.toCommentResponseDto(updatedComment);
    }

    @DeleteMapping("/users/{userId}/comments/{eventId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer userId,
                       @PathVariable Integer eventId) {
        log.info("PRIVATE UPDATE");
        log.info("Request for delete Comment: userId = {}; eventId = {}", userId, eventId);
        commentService.delete(userId, eventId);
    }

    @GetMapping("/users/{userId}/comments/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public CommentResponseDto getByEventId(@PathVariable int userId,
                                           @PathVariable int eventId) {
        log.info("PRIVATE GET");
        log.info("Request for get Comment: userId = {}; eventId = {}", userId, eventId);
        Comment comment = commentService.getByEventId(userId, eventId);

        return CommentMapper.toCommentResponseDto(comment);
    }

    @GetMapping("/users/{userId}/comments")
    @ResponseStatus(HttpStatus.OK)
    public List<CommentResponseDto> getAllByUserId(@PathVariable int userId,
                                                   @RequestParam(defaultValue = "0", required = false) @Min(0) int from,
                                                   @RequestParam(defaultValue = "10", required = false) @Min(1) int size) {
        log.info("PRIVATE GET ALL");
        log.info("Request for get Comments: userId = {}; from = {}, size = {}", userId, from, size);
        List<Comment> comments = commentService.getAllByUserId(userId, from, size);

        return comments.stream()
                .map(CommentMapper::toCommentResponseDto)
                .collect(Collectors.toList());
    }
//private path end

//admin path start
    @GetMapping("/admin/comments")
    @ResponseStatus(HttpStatus.OK)
    public List<CommentResponseDto> getAll(@RequestParam(required = false) List<Integer> userIds,
                                           @RequestParam(required = false) List<Integer> eventIds,
                                           @RequestParam(required = false) boolean isApproved,
                                           @RequestParam(required = false) String rangeStart,
                                           @RequestParam(required = false) String rangeEnd,
                                           @RequestParam(defaultValue = "0", required = false) @Min(0) int from,
                                           @RequestParam(defaultValue = "10", required = false) @Min(1) int size) {
        log.info("ADMIN GET ALL");
        log.info("Request for get Comments:" +
                "\nusers Id = {};" +
                "\nevents Id = {};" +
                "\napproved = {};" +
                "\ndate start search = {};" +
                "\ndate end search = {};" +
                "\nfrom = {};" +
                "\nsize = {};", userIds, eventIds, isApproved, rangeStart, rangeEnd, from, size);

        LocalDateTime commentCreatedStart = Optional.ofNullable(rangeStart)
                .map(DateTimeUtils::parse)
                .orElse(null);
        LocalDateTime commentCreatedEnd = Optional.ofNullable(rangeEnd)
                .map(DateTimeUtils::parse)
                .orElse(null);

        return commentService.getAllByUsersAndEventsAndApprovedAndPublishedOn(
                userIds,
                eventIds,
                isApproved,
                commentCreatedStart,
                commentCreatedEnd,
                from,
                size).stream()
                .map(CommentMapper::toCommentResponseDto)
                .collect(Collectors.toList());
    }

    @PatchMapping("/admin/comments/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public CommentResponseDto approve(@PathVariable int commentId) {
        log.info("ADMIN SET APPROVE");
        Comment comment = commentService.getById(commentId);
        comment.setIsApproved(true);
        Comment approvedComment = commentService.update(comment);

        return CommentMapper.toCommentResponseDto(approvedComment);
    }
//admin path end

//common path
    @GetMapping("/public/comments/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public List<CommentResponseDto> getAll(@PathVariable int eventId,
                                          @RequestParam(defaultValue = "0", required = false) @Min(0) int from,
                                          @RequestParam(defaultValue = "10", required = false) @Min(1) int size) {
        log.info("COMMON GET ALL");
        log.info("Request for get Comments for event {}", eventId);

        return commentService.getAllByEventId(eventId, from, size)
                .stream()
                .map(CommentMapper::toCommentResponseDto)
                .collect(Collectors.toList());
    }
}
