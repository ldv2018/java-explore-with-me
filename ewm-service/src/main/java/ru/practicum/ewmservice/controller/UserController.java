package ru.practicum.ewmservice.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewmservice.dto.UserDto;
import ru.practicum.ewmservice.mapper.UserMapper;
import ru.practicum.ewmservice.model.User;
import ru.practicum.ewmservice.service.UserService;

import javax.validation.constraints.Min;
import java.util.List;

@RestController
@RequestMapping(path = "/admin/users")
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j
public class UserController {
    final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User add(@RequestBody @Validated UserDto userDto) {
        log.info("Request for add User: {}", userDto);
        User user = UserMapper.toUser(userDto);

        return userService.add(user);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<User> getAll(@RequestParam(required = false) List<Integer> ids,
                             @RequestParam(required = false, defaultValue = "0") @Min(0) int from,
                             @RequestParam(required = false, defaultValue = "10") @Min(1) int size) {
        log.info("Request for get all users with ids = {}, from = {}, size = {}", ids, from, size);
        List<User> result = ids != null || !ids.isEmpty()
                ? userService.findAllById(ids, from, size)
                : userService.findAll(from, size);

        return result;
    }

    @DeleteMapping("{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable int userId) {
        log.info("Request for delete user with id = {}", userId);
        userService.delete(userId);
    }
}
