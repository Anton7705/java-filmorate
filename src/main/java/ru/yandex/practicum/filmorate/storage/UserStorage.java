package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

public interface UserStorage {
    void save(User user);

    List<User> getAll();

    Optional<User> findById(long id);

    List<User> getAllFriends(User user);

    List<User> getListOfUsers(List<Long> list);
}
