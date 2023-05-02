package ru.practicum.ewmservice.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.validation.ValidationException;
import java.time.LocalDateTime;

import static ru.practicum.ewmservice.exception.ErrorCode.*;
import static ru.practicum.ewmservice.util.DateTimeUtils.format;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @Data
    @Builder
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    private static class ErrorResponse {
        String status;
        String reason;
        String message;
        String timestamp;
    }

    private final ObjectMapper mapper = new ObjectMapper();

    @ExceptionHandler({
            ValidationException.class,
            MethodArgumentNotValidException.class,
            IllegalArgumentException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleValidationException(Throwable exc) {
        log.error(exc.getClass().getSimpleName(), exc.getMessage());

        ErrorResponse response = prepareResponse(BAD_REQUEST, "Incorrectly made request.", exc);
        return safeResponse(response);
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFoundException(NotFoundException exc) {
        log.error(exc.getClass().getSimpleName(), exc.getMessage());

        ErrorResponse response = prepareResponse(NOT_FOUND, "The required object was not found.", exc);
        return safeResponse(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleDataIntegrityViolationException(DataIntegrityViolationException exc) {
        log.error(exc.getClass().getSimpleName(), exc.getMessage());

        ErrorResponse response = prepareResponse(CONFLICT, "Integrity constraint has been violated.", exc);
        return safeResponse(response);
    }

    @ExceptionHandler(ForbiddenOperation.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleDataIntegrityViolationException(ForbiddenOperation exc) {
        log.error(exc.getClass().getSimpleName(), exc.getMessage());

        ErrorResponse response = prepareResponse(CONFLICT, "For the requested operation the conditions are not met.", exc);
        return safeResponse(response);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleException(Throwable exc) {
        log.error(exc.getClass().getSimpleName(), exc.getMessage());
        exc.printStackTrace();

        return ResponseEntity.internalServerError().toString();
    }

    private String safeResponse(ErrorResponse response) {
        try {
            return mapper.writeValueAsString(response);
        } catch (JsonProcessingException ex) {
            return ResponseEntity.internalServerError().toString();
        }
    }

    private ErrorResponse prepareResponse(
            ErrorCode code,
            String reason,
            Throwable exc) {
        String timestamp = format(LocalDateTime.now());
        return ErrorResponse.builder()
                .status(code.name())
                .reason(reason)
                .message(exc.getMessage())
                .timestamp(timestamp)
                .build();
    }
}
