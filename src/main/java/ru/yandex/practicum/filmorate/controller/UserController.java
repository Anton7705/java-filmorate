package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<User> getUsers() {
        log.info("Запрос коллекции пользователей");
        return userService.getUsers();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User createUser(@Valid @RequestBody User user) {
        log.info("Запрос на добавление пользователя");
        return userService.create(user);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public User updateUser(@Valid @RequestBody User user) {
        log.info("Запрос на обновление пользователя с id={}", user.getId());
        return userService.update(user.getId(), user);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public User getUser(@PathVariable long id) {
        log.info("Запрос пользователя с id={}", id);
        return userService.getUser(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    @ResponseStatus(HttpStatus.OK)
    public void addFriendToUser(@PathVariable long id, @PathVariable long friendId) {
        log.info("Запрос на добавление друга {} пользователю {}", friendId, id);
        userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFriendFromUser(@PathVariable long id, @PathVariable long friendId) {
        log.info("Запрос на удаление друга {} у пользователя {}", friendId, id);
        userService.deleteFriend(id, friendId);
    }

    @GetMapping("{id}/friends")
    @ResponseStatus(HttpStatus.OK)
    public List<User> getAllUsersFriends(@PathVariable long id) {
        log.info("Запрос на получение списка друзей у пользователя {}", id);
        return userService.getAllUsersFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    @ResponseStatus(HttpStatus.OK)
    public List<User> getAllUsersCommonFriends(@PathVariable long id, @PathVariable long otherId) {
        log.info("Запрос на получение общего списка друзей у пользователя {} и {}", id, otherId);
        return userService.getAllUsersCommonFriends(id, otherId);
    }
}
