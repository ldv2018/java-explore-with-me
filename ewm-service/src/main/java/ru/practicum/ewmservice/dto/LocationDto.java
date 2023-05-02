package ru.practicum.ewmservice.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public class LocationDto {
    @NotNull
    float lat;
    @NotNull
    float lon;
}
