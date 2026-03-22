package ru.yandex.practicum.filmorate.storage.memory;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private long nextId = 1;

    @Override
    public void save(User user) {
        if (user.getId() == 0) {
            user.setId(nextId++);
        }
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

    @Override
    public void addFriendship(long userId, long friendId) {
        User user = users.get(userId);
        user.addFriendId(friendId);
    }

    @Override
    public void removeFriendship(long userId, long friendId) {
        User user = users.get(userId);
        user.removeFriendId(friendId);
    }
}
