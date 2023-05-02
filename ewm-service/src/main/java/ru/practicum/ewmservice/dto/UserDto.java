package ru.practicum.ewmservice.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserDto {
    @NotNull
    String name;
    @NotNull
    @Email
    String email;
}
