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
    private long count = 1;

    public List<User> getUsers() {
        return userStorage.getAll();
    }

    public User getUser(long id) {
        return validationService.getUserOrThrow(id);
    }

    public User create(User user) {
        setNameFromLoginIfBlank(user);
        user.setId(count++);
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
        User user = validationService.getUserOrThrow(id);
        User friend = validationService.getUserOrThrow(friendId);
        if (id == friendId) {
            log.warn("Попытка добавить самого себя в друзья");
            throw new ValidationException("Нельзя добавить самого себя в друзья");
        }
        if (user.getFriendsIds().contains(friendId) || friend.getFriendsIds().contains(id)) {
            log.warn("Попытка добавить одного и того же пользователя в друзья дважды");
            throw new ValidationException("Пользователи уже друзья");
        }
        user.addFriendId(friendId);
        friend.addFriendId(id);
        userStorage.save(user);
        userStorage.save(friend);
        log.debug("Пользователь c id {} и {} теперь друзья", id, friendId);
    }

    public void deleteFriend(long id, long friendId) {
        User user = validationService.getUserOrThrow(id);
        User friend = validationService.getUserOrThrow(friendId);

        if (!user.getFriendsIds().contains(friendId) || !friend.getFriendsIds().contains(id)) {
            log.debug("Попытка разорвать дружбу между {} и {} - операция проигнорирована", id, friendId);
            return;
        }
        user.removeFriendId(friendId);
        friend.removeFriendId(id);
        userStorage.save(user);
        userStorage.save(friend);
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
        User user = validationService.getUserOrThrow(id);
        User otherUser = validationService.getUserOrThrow(otherId);
        Set<Long> setOfId = new HashSet<>(user.getFriendsIds());
        setOfId.retainAll(otherUser.getFriendsIds());
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
