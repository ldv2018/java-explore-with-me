package ru.practicum.ewmservice.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.practicum.ewmservice.exception.NotFoundException;
import ru.practicum.ewmservice.model.Comment;
import ru.practicum.ewmservice.storage.CommentRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentService {

    final CommentRepository commentRepository;

    public Comment add(Comment comment) {
        comment.setIsApproved(false);
        comment.setIsEdited(false);
        comment.setPublishedOn(LocalDateTime.now());

        return commentRepository.save(comment);
    }

    public Comment update(Comment comment) {
        comment.setIsEdited(true);

        return commentRepository.save(comment);
    }

    public void delete(int userId, int eventId) {
        Comment comment = commentRepository.findByUserIdAndEventId(userId, eventId)
                .orElseThrow(() -> new NotFoundException(HttpStatus.NOT_FOUND, "User has no comment for this event"));
        commentRepository.delete(comment);
    }

    public Comment getByEventId(int userId, int eventId) {
        return commentRepository.findByUserIdAndEventId(userId, eventId)
                .orElseThrow(() -> new NotFoundException(HttpStatus.NOT_FOUND, "User has no comment for this event"));
    }

    public List<Comment> getAllByUserId(int userId, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);

        return commentRepository.findAllByUserId(userId, pageable)
                .getContent();
    }

    public List<Comment> getAllByUsersAndEventsAndApprovedAndPublishedOn(
            List<Integer> userIds,
            List<Integer> eventIds,
            boolean isApproved,
            LocalDateTime commentCreatedStart,
            LocalDateTime commentCreatedEnd,
            int from,
            int size) {
        Pageable pageable = PageRequest.of(from / size, size);

        return commentRepository.getAllByUsersAndEventsAndApprovedAndPublishedOn(
                userIds,
                eventIds,
                isApproved,
                commentCreatedStart,
                commentCreatedEnd,
                pageable)
                .getContent();
    }

    public Comment getById(int commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(HttpStatus.NOT_FOUND, "Comment not found"));
    }

    public List<Comment> getAllByEventId(int eventId, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);

        return commentRepository.getALlByEventIdAndApproved(eventId, pageable)
                .getContent();
    }
}
