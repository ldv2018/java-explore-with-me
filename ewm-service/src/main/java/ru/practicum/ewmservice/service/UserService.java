package ru.practicum.ewmservice.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.practicum.ewmservice.exception.NotFoundException;
import ru.practicum.ewmservice.model.User;
import ru.practicum.ewmservice.storage.UserRepository;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserService {
    final UserRepository userRepository;
    public User add(User user) {
        return userRepository.save(user);
    }

    public User findById(int id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(HttpStatus.NOT_FOUND,
                        "User with id= " + id + " was not found"));
    }

    public List<User> findAllById(List<Integer> ids, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);

        return userRepository
                .findByIdInOrderByIdDesc(ids, pageable)
                .getContent();
    }

    public List<User> findAll(int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);

        return userRepository
                .findAll(pageable)
                .getContent();
    }

    public void delete(int id) {
        throwIfUserNotExist(id);
        userRepository.deleteById(id);
    }

    private void throwIfUserNotExist(int id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException(HttpStatus.NOT_FOUND, "User with id= " + id + " was not found");
        }
    }
}
