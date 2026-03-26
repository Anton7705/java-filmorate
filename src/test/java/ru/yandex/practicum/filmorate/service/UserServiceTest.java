package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.db.*;
import ru.yandex.practicum.filmorate.storage.db.rowmapper.*;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@Sql(scripts = {"/schema.sql", "/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class UserServiceTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private UserService userService;
    private ValidationService validationService;
    private UserDbStorage userStorage;
    private FilmDbStorage filmStorage;
    private MpaDbStorage mpaStorage;
    private GenreDbStorage genreStorage;

    private User correctUser;

    @BeforeEach
    void setUp() {
        // Инициализация хранилищ
        filmStorage = new FilmDbStorage(jdbcTemplate, new FilmRowMapper());
        userStorage = new UserDbStorage(jdbcTemplate, new UserRowMapper());
        mpaStorage = new MpaDbStorage(jdbcTemplate, new MpaRowMapper());
        genreStorage = new GenreDbStorage(jdbcTemplate, new GenreRowMapper(), namedParameterJdbcTemplate);

        validationService = new ValidationService(userStorage, filmStorage, mpaStorage, genreStorage);
        userService = new UserService(userStorage, validationService);

        correctUser = User.builder()
                .email("mail@mail.ru")
                .login("login")
                .name("name")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
    }

    @Test
    @DisplayName("Присвоение ID новому пользователю")
    void shouldAssignIdWhenCreatingUser() {
        userService.create(correctUser);
        assertThat(correctUser.getId()).isPositive();
    }

    @Test
    @DisplayName("Создание пользователя: корректные данные -> пользователь сохраняется")
    void shouldSaveUserWhenDataIsValid() {
        User created = userService.create(correctUser);

        List<User> users = userService.getUsers();

        assertThat(users)
                .hasSize(1)
                .first()
                .satisfies(user -> {
                    assertThat(user.getId()).isEqualTo(created.getId());
                    assertThat(user.getEmail()).isEqualTo("mail@mail.ru");
                    assertThat(user.getLogin()).isEqualTo("login");
                });
    }

    @Test
    @DisplayName("Установка имени: если name пустой или null, используется login")
    void shouldSetNameFromLoginWhenNameIsBlank() {
        User userWithBlankName = User.builder()
                .email("mail@mail.ru")
                .login("login")
                .name("")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
        User userWithNullName = User.builder()
                .email("mail@mail.ru")
                .login("login")
                .name(null)
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User createdWithBlankName = userService.create(userWithBlankName);
        User createdWithNullName = userService.create(userWithNullName);

        assertThat(createdWithBlankName.getName()).isEqualTo("login");
        assertThat(createdWithNullName.getName()).isEqualTo("login");
    }

    @Test
    @DisplayName("Обновление пользователя: данные успешно обновляются")
    void shouldUpdateUserSuccessfully() {
        User created = userService.create(correctUser);
        long id = created.getId();

        User userToUpdate = User.builder()
                .email("new@mail.ru")
                .login("newlogin")
                .name("NewName")
                .birthday(LocalDate.of(1985, 5, 15))
                .build();

        User updatedUser = userService.update(id, userToUpdate);

        assertThat(updatedUser.getEmail()).isEqualTo("new@mail.ru");
        assertThat(updatedUser.getLogin()).isEqualTo("newlogin");
        assertThat(updatedUser.getName()).isEqualTo("NewName");
    }

    @Test
    @DisplayName("Обновление: несуществующий id -> исключение")
    void shouldThrowExceptionWhenUpdatingNonExistingUser() {
        User userToUpdate = User.builder()
                .email("mail@mail.ru")
                .login("login")
                .name("name")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        assertThrows(NotFoundException.class,
                () -> userService.update(999L, userToUpdate));
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

        assertThat(user1Friends).hasSize(1);
        assertThat(user1Friends.get(0).getId()).isEqualTo(user2.getId());
        assertThat(user2Friends).isEmpty();  // односторонняя дружба
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

        assertThat(user1Friends).isEmpty();
        assertThat(user2Friends).isEmpty();
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
        assertThat(user1Friends).isEmpty();
    }

    @Test
    @DisplayName("Получение списка друзей: у пользователя нет друзей")
    void shouldReturnEmptyListWhenUserHasNoFriends() {
        User user = userService.create(correctUser);

        List<User> friends = userService.getAllUsersFriends(user.getId());

        assertThat(friends).isEmpty();
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

        assertThat(commonFriends).hasSize(1);
        assertThat(commonFriends.get(0).getId()).isEqualTo(commonFriend.getId());
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

        assertThat(commonFriends).isEmpty();
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

        assertThat(found.getId()).isEqualTo(created.getId());
        assertThat(found.getEmail()).isEqualTo(created.getEmail());
    }

    @Test
    @DisplayName("Получение пользователя по ID: несуществующий ID -> исключение")
    void shouldThrowExceptionWhenGettingNonExistingUser() {
        assertThrows(NotFoundException.class,
                () -> userService.getUser(999L));
    }
}