package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
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
        validateUser(user);

        setNameFromLoginIfBlank(user);
        user.setId(count++);
        users.put(user.getId(), user);
        log.debug("Пользователь успешно добавлен в список");
        return user;
    }

    public User update(long id, User updatedUser) {
        if (!users.containsKey(id)) {
            throw new ValidationException("Отсутствует пользователь с id=" + id);
        }
        validateUser(updatedUser);

        setNameFromLoginIfBlank(updatedUser);
        updatedUser.setId(id);
        users.put(id, updatedUser);
        log.debug("Пользователь успешно обновлен");
        return updatedUser;
    }

    private void validateUser(User user) {
        String email = user.getEmail();
        if (email == null || email.isBlank() || !email.contains("@")) {
            log.warn("Валидация не пройдена: некорректный email '{}'", email);
            throw new ValidationException("email пуст или не содержит @");
        }
        String login = user.getLogin();
        if (login == null || login.isBlank() || login.contains(" ")) {
            log.warn("Валидация не пройдена: некорректный login '{}'", login);
            throw new ValidationException("Login пуст или содержит пробелы");
        }
        LocalDate birthday = user.getBirthday();
        if (birthday == null) {
            log.warn("Валидация не пройдена: дата рождения пуста");
            throw new ValidationException("Дата рождения не может быть пуста");
        }
        if (birthday.isAfter(LocalDate.now())) {
            log.warn("Валидация не пройдена: дата рождения {} в будущем", birthday);
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }

    private void setNameFromLoginIfBlank(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            log.info("Пользователю присвоен логин в качестве имени");
            user.setName(user.getLogin());
        }
    }
}
