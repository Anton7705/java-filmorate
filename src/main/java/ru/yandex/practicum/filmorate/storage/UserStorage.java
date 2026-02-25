package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {
    void save(User user);

    List<User> getAll();

    boolean hasUserWithId(long id);
}
