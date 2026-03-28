package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;
    private final ValidationService validationService;

    public List<User> getUsers() {
        return userStorage.getAll();
    }

    public User getUser(long id) {
        return validationService.getUserOrThrow(id);
    }

    public User create(User user) {
        setNameFromLoginIfBlank(user);
        userStorage.save(user);
        log.debug("Пользователь успешно добавлен в список");
        return user;
    }

    public User update(long id, User updatedUser) {
        validationService.getUserOrThrow(id);

        setNameFromLoginIfBlank(updatedUser);
        updatedUser.setId(id);
        userStorage.save(updatedUser);
        log.debug("Пользователь успешно обновлен");
        return updatedUser;
    }

    public void addFriend(long id, long friendId) {
        if (id == friendId) {
            throw new ValidationException("Нельзя добавить самого себя в друзья");
        }
        Map<Long, User> users = userStorage.getUsersMapByIds(List.of(id, friendId));
        User user = users.get(id);
        User friend = users.get(friendId);
        userStorage.addFriendship(user.getId(), friend.getId());
        log.debug("Пользователь c id {} и {} теперь друзья", id, friendId);
    }

    public void deleteFriend(long id, long friendId) {
        Map<Long, User> users = userStorage.getUsersMapByIds(List.of(id, friendId));
        User user = users.get(id);
        User friend = users.get(friendId);

        if (!userStorage.getAllFriends(user).contains(friend)) {
            log.warn("Попытка удалить друга {} у пользователя {} - операция проигнорирована", friendId, id);
            return;
        }
        userStorage.removeFriendship(user.getId(), friend.getId());
        log.debug("Пользователь c id {} и {} больше не друзья", id, friendId);
    }

    public List<User> getAllUsersFriends(long id) {
        User user = validationService.getUserOrThrow(id);
        List<User> friends = userStorage.getAllFriends(user);
        log.debug("Получен список друзей пользователя {}", id);
        return friends;
    }

    public List<User> getAllUsersCommonFriends(long id, long otherId) {
        if (id == otherId) {
            log.warn("Попытка запросить общих друзей у самого себя");
            throw new ValidationException("Нельзя запросить общих друзей у самого себя");
        }
        Map<Long, User> users = userStorage.getUsersMapByIds(List.of(id, otherId));
        User user = users.get(id);
        User otherUser = users.get(otherId);
        List<Long> userFriends = userStorage.getAllFriends(user)
                .stream()
                .map(User::getId)
                .toList();

        List<Long> otherUserFriends = userStorage.getAllFriends(otherUser)
                .stream()
                .map(User::getId)
                .toList();

        Set<Long> setOfId = new HashSet<>(userFriends);
        setOfId.retainAll(otherUserFriends);
        List<User> commonFriends = userStorage.getListOfUsers(new ArrayList<>(setOfId));
        log.debug("Получен список общих друзей пользователя {} с пользователем", id);
        if (commonFriends.isEmpty()) {
            return Collections.emptyList();
        }
        return commonFriends;
    }

    private void setNameFromLoginIfBlank(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            log.info("Пользователю присвоен логин в качестве имени");
            user.setName(user.getLogin());
        }
    }
}
