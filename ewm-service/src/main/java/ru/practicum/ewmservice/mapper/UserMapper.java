package ru.practicum.ewmservice.mapper;

import lombok.NoArgsConstructor;
import ru.practicum.ewmservice.dto.UserDto;
import ru.practicum.ewmservice.model.User;

@NoArgsConstructor
public class UserMapper {
    public static User toUser(UserDto userDto) {
        return User.builder()
                .email(userDto.getEmail())
                .name(userDto.getName())
                .build();
    }
}
