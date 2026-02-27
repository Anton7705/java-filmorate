package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public void save(User user) {
        users.put(user.getId(), user);
    }

    @Override
    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public Optional<User> findById(long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public List<User> getAllFriends(User user) {
        List<User> friends = new ArrayList<>();
        for (Long friendsId : user.getFriendsIds()) {
            friends.add(users.get(friendsId));
        }
        return friends;
    }

    @Override
    public List<User> getListOfUsers(List<Long> list) {
        List<User> friends = new ArrayList<>();
        for (Long aLong : list) {
            User user = users.get(aLong);
            if (user == null) {
                throw new NotFoundException("Пользователь с id=" + aLong + " не найден");
            }
            friends.add(user);
        }
        return friends;
    }
}
