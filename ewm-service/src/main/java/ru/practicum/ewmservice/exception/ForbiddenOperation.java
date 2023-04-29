package ru.practicum.ewmservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ForbiddenOperation extends ResponseStatusException {
    public ForbiddenOperation(HttpStatus status, String reason) {
        super(status, reason);
    }
}
