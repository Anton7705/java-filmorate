package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private static UserService userService;
    private static User correctUser;
    private static LocalDate birthday = LocalDate.of(1990, 1, 1);

    @BeforeEach
    void setup() {
        FilmStorage filmStorage = new InMemoryFilmStorage();
        UserStorage userStorage = new InMemoryUserStorage();
        ValidationService validationService = new ValidationService(userStorage, filmStorage);
        userService = new UserService(userStorage, validationService);
        correctUser = User.builder()
                .email("mail@mail.ru")
                .login("login")
                .name("name")
                .birthday(birthday)
                .build();
    }

    @Test
    @DisplayName("Присвоение ID новому пользователю")
    void shouldAssignIdWhenCreatingUser() {
        userService.create(correctUser);
        assertEquals(correctUser.getId(), 1);
    }

    @Test
    @DisplayName("Создание пользователя: корректные данные -> пользователь сохраняется")
    void shouldSaveUserWhenDataIsValid() {
        User created = userService.create(correctUser);

        assertTrue(userService.getUsers().contains(created));
        assertEquals(1, userService.getUsers().size());
    }

    @Test
    @DisplayName("Установка имени: если name пустой или null, используется login")
    void shouldSetNameFromLoginWhenNameIsBlank() {
        User userWithBlankName = User.builder()
                .email("mail@mail.ru")
                .login("login")
                .name("")
                .birthday(birthday)
                .build();
        User userWithNullName = User.builder()
                .email("mail@mail.ru")
                .login("login")
                .name(null)
                .birthday(birthday)
                .build();

        User createdWithBlankName = userService.create(userWithBlankName);
        User createdWithNullName = userService.create(userWithNullName);

        assertEquals("login", createdWithBlankName.getName());
        assertEquals("login", createdWithNullName.getName());
    }

    @Test
    @DisplayName("Обновление пользователя: данные успешно обновляются")
    void shouldUpdateUserSuccessfully() {
        userService.create(correctUser);
        long id = correctUser.getId();
        User userToUpdate = User.builder()
                .email("new@mail.ru")
                .login("newlogin")
                .name("NewName")
                .birthday(LocalDate.of(1985, 5, 15))
                .build();
        User updatedUser = userService.update(id, userToUpdate);
        assertEquals(userToUpdate.getEmail(), updatedUser.getEmail());
        assertEquals(userToUpdate.getLogin(), updatedUser.getLogin());
    }

    @Test
    @DisplayName("Обновление: несуществующий id -> исключение")
    void shouldThrowExceptionWhenUpdatingNonExistingUser() {
        User userToUpdate = User.builder()
                .email("mail@mail.ru")
                .login("login")
                .name("name")
                .birthday(birthday)
                .build();

        assertThrows(NotFoundException.class, () -> userService.update(999L, userToUpdate));
    }

    @Test
    @DisplayName("Добавление в друзья: успешное добавление")
    void shouldAddFriendSuccessfully() {
        User user1 = userService.create(correctUser);
        User user2 = userService.create(User.builder()
                .email("friend@mail.ru")
                .login("friend")
                .name("Friend")
                .birthday(LocalDate.of(1995, 5, 5))
                .build());

        userService.addFriend(user1.getId(), user2.getId());

        List<User> user1Friends = userService.getAllUsersFriends(user1.getId());
        List<User> user2Friends = userService.getAllUsersFriends(user2.getId());

        assertEquals(1, user1Friends.size());
        assertEquals(1, user2Friends.size());
        assertEquals(user2.getId(), user1Friends.get(0).getId());
        assertEquals(user1.getId(), user2Friends.get(0).getId());
    }

    @Test
    @DisplayName("Добавление в друзья: попытка добавить себя -> исключение")
    void shouldThrowExceptionWhenAddingSelfAsFriend() {
        User user = userService.create(correctUser);
        assertThrows(ValidationException.class,
                () -> userService.addFriend(user.getId(), user.getId()));
    }

    @Test
    @DisplayName("Добавление в друзья: повторное добавление -> исключение")
    void shouldThrowExceptionWhenAddingDuplicateFriend() {
        User user1 = userService.create(correctUser);
        User user2 = userService.create(User.builder()
                .email("friend@mail.ru")
                .login("friend")
                .name("Friend")
                .birthday(LocalDate.of(1995, 5, 5))
                .build());

        userService.addFriend(user1.getId(), user2.getId());

        assertThrows(ValidationException.class,
                () -> userService.addFriend(user1.getId(), user2.getId()));
    }

    @Test
    @DisplayName("Добавление в друзья: несуществующий пользователь -> исключение")
    void shouldThrowExceptionWhenAddingNonExistingUserAsFriend() {
        User user = userService.create(correctUser);

        assertThrows(NotFoundException.class,
                () -> userService.addFriend(user.getId(), 999L));
    }

    @Test
    @DisplayName("Удаление из друзей: успешное удаление")
    void shouldDeleteFriendSuccessfully() {
        User user1 = userService.create(correctUser);
        User user2 = userService.create(User.builder()
                .email("friend@mail.ru")
                .login("friend")
                .name("Friend")
                .birthday(LocalDate.of(1995, 5, 5))
                .build());

        userService.addFriend(user1.getId(), user2.getId());
        userService.deleteFriend(user1.getId(), user2.getId());

        List<User> user1Friends = userService.getAllUsersFriends(user1.getId());
        List<User> user2Friends = userService.getAllUsersFriends(user2.getId());

        assertTrue(user1Friends.isEmpty());
        assertTrue(user2Friends.isEmpty());
    }

    @Test
    @DisplayName("Удаление из друзей: удаление друга которого нет")
    void shouldNotThrowExceptionWhenDeletingNonExistingFriendship() {
        User user1 = userService.create(correctUser);
        User user2 = userService.create(User.builder()
                .email("friend@mail.ru")
                .login("friend")
                .name("Friend")
                .birthday(LocalDate.of(1995, 5, 5))
                .build());

        assertDoesNotThrow(() -> userService.deleteFriend(user1.getId(), user2.getId()));

        List<User> user1Friends = userService.getAllUsersFriends(user1.getId());
        assertTrue(user1Friends.isEmpty());
    }

    @Test
    @DisplayName("Получение списка друзей: у пользователя нет друзей")
    void shouldReturnEmptyListWhenUserHasNoFriends() {
        User user = userService.create(correctUser);

        List<User> friends = userService.getAllUsersFriends(user.getId());

        assertTrue(friends.isEmpty());
    }

    @Test
    @DisplayName("Получение списка друзей: несуществующий пользователь -> исключение")
    void shouldThrowExceptionWhenGettingFriendsForNonExistingUser() {
        assertThrows(NotFoundException.class,
                () -> userService.getAllUsersFriends(999L));
    }

    @Test
    @DisplayName("Получение общих друзей: успешное получение")
    void shouldGetCommonFriendsSuccessfully() {
        User user1 = userService.create(correctUser);
        User user2 = userService.create(User.builder()
                .email("user2@mail.ru")
                .login("user2")
                .name("User2")
                .birthday(LocalDate.of(1992, 2, 2))
                .build());
        User commonFriend = userService.create(User.builder()
                .email("common@mail.ru")
                .login("common")
                .name("Common")
                .birthday(LocalDate.of(1993, 3, 3))
                .build());

        userService.addFriend(user1.getId(), commonFriend.getId());
        userService.addFriend(user2.getId(), commonFriend.getId());

        List<User> commonFriends = userService.getAllUsersCommonFriends(user1.getId(), user2.getId());

        assertEquals(1, commonFriends.size());
        assertEquals(commonFriend.getId(), commonFriends.get(0).getId());
    }

    @Test
    @DisplayName("Получение общих друзей: нет общих друзей")
    void shouldReturnEmptyListWhenNoCommonFriends() {
        User user1 = userService.create(correctUser);
        User user2 = userService.create(User.builder()
                .email("user2@mail.ru")
                .login("user2")
                .name("User2")
                .birthday(LocalDate.of(1992, 2, 2))
                .build());
        User friend1 = userService.create(User.builder()
                .email("friend1@mail.ru")
                .login("friend1")
                .name("Friend1")
                .birthday(LocalDate.of(1993, 3, 3))
                .build());
        User friend2 = userService.create(User.builder()
                .email("friend2@mail.ru")
                .login("friend2")
                .name("Friend2")
                .birthday(LocalDate.of(1994, 4, 4))
                .build());

        userService.addFriend(user1.getId(), friend1.getId());
        userService.addFriend(user2.getId(), friend2.getId());

        List<User> commonFriends = userService.getAllUsersCommonFriends(user1.getId(), user2.getId());

        assertTrue(commonFriends.isEmpty());
    }

    @Test
    @DisplayName("Получение общих друзей: попытка получить общих друзей с самим собой -> исключение")
    void shouldThrowExceptionWhenGettingCommonFriendsWithSelf() {
        User user = userService.create(correctUser);

        assertThrows(ValidationException.class,
                () -> userService.getAllUsersCommonFriends(user.getId(), user.getId()));
    }

    @Test
    @DisplayName("Получение пользователя по ID: успешное получение")
    void shouldGetUserByIdSuccessfully() {
        User created = userService.create(correctUser);

        User found = userService.getUser(created.getId());

        assertEquals(created.getId(), found.getId());
        assertEquals(created.getEmail(), found.getEmail());
    }

    @Test
    @DisplayName("Получение пользователя по ID: несуществующий ID -> исключение")
    void shouldThrowExceptionWhenGettingNonExistingUser() {
        assertThrows(NotFoundException.class,
                () -> userService.getUser(999L));
    }
}
