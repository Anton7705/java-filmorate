package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private static UserService userService;
    private static User correctUser;
    private static LocalDate birthday = LocalDate.of(1990, 1, 1);

    @BeforeEach
    void setup() {
        userService = new UserService();
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
    @DisplayName("Валидация email: пустой, null или отсутствует -> исключение")
    void shouldThrowExceptionWhenEmailIsInvalid() {
        User userWithoutEmail = User.builder()
                .login("login")
                .name("name")
                .birthday(birthday)
                .build();
        User userWithBlankEmail = User.builder()
                .email("")
                .login("login")
                .name("name")
                .birthday(birthday)
                .build();
        User userWithNullEmail = User.builder()
                .email(null)
                .login("login")
                .name("name")
                .birthday(birthday)
                .build();

        assertThrows(ValidationException.class, () -> userService.create(userWithoutEmail));
        assertThrows(ValidationException.class, () -> userService.create(userWithBlankEmail));
        assertThrows(ValidationException.class, () -> userService.create(userWithNullEmail));
    }

    @Test
    @DisplayName("Валидация email: без @ -> исключение")
    void shouldThrowExceptionWhenEmailDoesNotContainAtSymbol() {
        User userWithInvalidEmail = User.builder()
                .email("invalid-email")
                .login("login")
                .name("name")
                .birthday(birthday)
                .build();

        assertThrows(ValidationException.class, () -> userService.create(userWithInvalidEmail));
    }

    @Test
    @DisplayName("Валидация login: пустой, null или отсутствует -> исключение")
    void shouldThrowExceptionWhenLoginIsInvalid() {
        User userWithoutLogin = User.builder()
                .email("mail@mail.ru")
                .name("name")
                .birthday(birthday)
                .build();
        User userWithBlankLogin = User.builder()
                .email("mail@mail.ru")
                .login("")
                .name("name")
                .birthday(birthday)
                .build();
        User userWithNullLogin = User.builder()
                .email("mail@mail.ru")
                .login(null)
                .name("name")
                .birthday(birthday)
                .build();

        assertThrows(ValidationException.class, () -> userService.create(userWithoutLogin));
        assertThrows(ValidationException.class, () -> userService.create(userWithBlankLogin));
        assertThrows(ValidationException.class, () -> userService.create(userWithNullLogin));
    }

    @Test
    @DisplayName("Валидация login: содержит пробелы -> исключение")
    void shouldThrowExceptionWhenLoginContainsSpaces() {
        User userWithLoginContainingSpaces = User.builder()
                .email("mail@mail.ru")
                .login("login with spaces")
                .name("name")
                .birthday(birthday)
                .build();

        assertThrows(ValidationException.class, () -> userService.create(userWithLoginContainingSpaces));
    }

    @Test
    @DisplayName("Валидация birthday: пустой, null или отсутствует -> исключение")
    void shouldThrowExceptionWhenBirthdayIsInvalid() {
        User userWithoutBirthday = User.builder()
                .email("mail@mail.ru")
                .login("login")
                .name("name")
                .build();
        User userWithNullBirthday = User.builder()
                .email("mail@mail.ru")
                .login("login")
                .name("name")
                .birthday(null)
                .build();

        assertThrows(ValidationException.class, () -> userService.create(userWithoutBirthday));
        assertThrows(ValidationException.class, () -> userService.create(userWithNullBirthday));
    }

    @Test
    @DisplayName("Валидация birthday: дата в будущем -> исключение")
    void shouldThrowExceptionWhenBirthdayIsInFuture() {
        User userWithFutureBirthday = User.builder()
                .email("mail@mail.ru")
                .login("login")
                .name("name")
                .birthday(LocalDate.now().plusYears(1))
                .build();

        assertThrows(ValidationException.class, () -> userService.create(userWithFutureBirthday));
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

        assertThrows(ValidationException.class, () -> userService.update(999L, userToUpdate));
    }
}
