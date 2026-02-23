package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
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
}
