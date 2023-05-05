package ru.practicum.ewmservice.mapper;

import lombok.NoArgsConstructor;
import ru.practicum.ewmservice.dto.CommentRequestDto;
import ru.practicum.ewmservice.dto.CommentResponseDto;
import ru.practicum.ewmservice.dto.EventCommentDto;
import ru.practicum.ewmservice.dto.UserResponseDto;
import ru.practicum.ewmservice.model.Comment;
import ru.practicum.ewmservice.util.DateTimeUtils;

@NoArgsConstructor
public class CommentMapper {
    public static Comment toComment(CommentRequestDto commentRequestDto) {
        return Comment.builder()
                .content(commentRequestDto.getContent())
                .build();
    }

    public static CommentResponseDto toCommentResponseDto(Comment comment) {
        UserResponseDto user = UserResponseDto.builder()
                .id(comment.getUser().getId())
                .name(comment.getUser().getName())
                .build();
        EventCommentDto event = EventCommentDto.builder()
                .id(comment.getEvent().getId())
                .name(comment.getEvent().getTitle())
                .build();

        return CommentResponseDto.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .user(user)
                .event(event)
                .isApproved(comment.getIsApproved())
                .isEdited(comment.getIsEdited())
                .publishedOn(DateTimeUtils.format(comment.getPublishedOn()))
                .build();
    }
}
