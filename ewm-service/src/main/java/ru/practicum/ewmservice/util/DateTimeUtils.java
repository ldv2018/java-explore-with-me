package ru.practicum.ewmservice.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DateTimeUtils {

    static final DateTimeFormatter DT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static LocalDateTime parse(@NonNull final String datetime) throws ValidationException {
        try {
            return LocalDateTime.parse(datetime, DT_FORMATTER);
        } catch (final DateTimeParseException e) {
            throw new ValidationException(e.getMessage(), e);
        }
    }

    public static String format(@NonNull final LocalDateTime datetime) {
        return datetime.format(DT_FORMATTER);
    }
}
