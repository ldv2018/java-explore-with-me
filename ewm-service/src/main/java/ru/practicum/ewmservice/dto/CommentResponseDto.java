package ru.practicum.ewmservice.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public class CommentResponseDto {
    int id;
    UserResponseDto user;
    EventCommentDto event;
    String content;
    String publishedOn;
    boolean isEdited;
    boolean isApproved;
}
