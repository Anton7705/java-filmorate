package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class UserService {

    private Map<Long, User> users = new HashMap<>();
    private long count = 1;

    public List<User> getUsers() {
        return new ArrayList<>(users.values());
    }

    public User create(User user) {
        setNameFromLoginIfBlank(user);
        user.setId(count++);
        users.put(user.getId(), user);
        log.debug("Пользователь успешно добавлен в список");
        return user;
    }

    public User update(long id, User updatedUser) {
        if (!users.containsKey(id)) {
            log.warn("Пользователь с id={} не найден", id);
            throw new NotFoundException("Отсутствует пользователь с id=" + id);
        }

        setNameFromLoginIfBlank(updatedUser);
        updatedUser.setId(id);
        users.put(id, updatedUser);
        log.debug("Пользователь успешно обновлен");
        return updatedUser;
    }

    private void setNameFromLoginIfBlank(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            log.info("Пользователю присвоен логин в качестве имени");
            user.setName(user.getLogin());
        }
    }
}
