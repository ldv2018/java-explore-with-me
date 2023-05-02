package ru.practicum.statsdto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Stat {
    String app;
    String uri;
    int hits;
}
