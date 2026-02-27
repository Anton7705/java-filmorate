package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User user1;
    private User user2;

    @BeforeEach
    void setup() {
        user1 = User.builder()
                .id(1L)
                .email("user1@mail.ru")
                .login("user1")
                .name("User One")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        user2 = User.builder()
                .id(2L)
                .email("user2@mail.ru")
                .login("user2")
                .name("User Two")
                .birthday(LocalDate.of(1995, 5, 5))
                .build();
    }

    @Test
    @DisplayName("addFriend: успешное добавление друга")
    void shouldAddFriendSuccessfully() {
        user1.addFriend(user2);

        assertTrue(user1.getFriendsIds().contains(user2.getId()));
    }

    @Test
    @DisplayName("addFriend: попытка добавить себя -> исключение")
    void shouldThrowExceptionWhenAddingSelf() {
        assertThrows(ValidationException.class, () -> user1.addFriend(user1));
    }

    @Test
    @DisplayName("addFriend: повторное добавление того же друга -> исключение")
    void shouldThrowExceptionWhenAddingDuplicateFriend() {
        user1.addFriend(user2);

        assertThrows(ValidationException.class, () -> user1.addFriend(user2));
    }

    @Test
    @DisplayName("deleteFriend: успешное удаление друга")
    void shouldDeleteFriendSuccessfully() {
        user1.addFriend(user2);
        user1.deleteFriend(user2);

        assertFalse(user1.getFriendsIds().contains(user2.getId()));
    }

    @Test
    @DisplayName("deleteFriend: удаление несуществующего друга (не падает)")
    void shouldNotThrowExceptionWhenDeletingNonExistingFriend() {
        assertDoesNotThrow(() -> user1.deleteFriend(user2));
    }

    @Test
    @DisplayName("getCommonFriendsIds: успешное получение общих друзей")
    void shouldGetCommonFriendsIdsSuccessfully() {
        User commonFriend = User.builder().id(3L).build();

        user1.addFriend(commonFriend);
        user2.addFriend(commonFriend);

        var commonIds = user1.getCommonFriendsIds(user2);

        assertEquals(1, commonIds.size());
        assertTrue(commonIds.contains(commonFriend.getId()));
    }

    @Test
    @DisplayName("getCommonFriendsIds: нет общих друзей")
    void shouldReturnEmptyListWhenNoCommonFriends() {
        User friend1 = User.builder().id(3L).build();
        User friend2 = User.builder().id(4L).build();

        user1.addFriend(friend1);
        user2.addFriend(friend2);

        var commonIds = user1.getCommonFriendsIds(user2);

        assertTrue(commonIds.isEmpty());
    }

    @Test
    @DisplayName("getCommonFriendsIds: попытка получить общих друзей с собой -> исключение")
    void shouldThrowExceptionWhenGettingCommonFriendsWithSelf() {
        assertThrows(ValidationException.class, () -> user1.getCommonFriendsIds(user1));
    }
}
