package ru.yandex.practicum.filmorate.storage.db;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.db.rowmapper.UserRowMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Sql(scripts = {"/schema.sql", "/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class UserDbStorageTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private UserDbStorage userStorage;

    @BeforeEach
    void setUp() {
        userStorage = new UserDbStorage(jdbcTemplate, new UserRowMapper());
    }

    @Test
    void shouldSaveAndFindUser() {
        User user = createTestUser();
        userStorage.save(user);

        Optional<User> found = userStorage.findById(user.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo(user.getEmail());
        assertThat(found.get().getLogin()).isEqualTo(user.getLogin());
    }

    @Test
    void shouldFindAllUsers() {
        userStorage.save(createTestUser());
        userStorage.save(createTestUser2());

        List<User> users = userStorage.getAll();

        assertThat(users).hasSize(2);
    }

    @Test
    void shouldUpdateUser() {
        User user = createTestUser();
        userStorage.save(user);

        user.setName("Updated Name");
        userStorage.save(user);

        Optional<User> updated = userStorage.findById(user.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getName()).isEqualTo("Updated Name");
    }

    @Test
    void shouldAddAndRemoveFriendship() {
        User user1 = createTestUser();
        User user2 = createTestUser2();
        userStorage.save(user1);
        userStorage.save(user2);

        userStorage.addFriendship(user1.getId(), user2.getId());

        List<User> friends = userStorage.getAllFriends(user1);
        assertThat(friends).hasSize(1);
        assertThat(friends.get(0).getId()).isEqualTo(user2.getId());

        userStorage.removeFriendship(user1.getId(), user2.getId());

        List<User> friendsAfterRemove = userStorage.getAllFriends(user1);
        assertThat(friendsAfterRemove).isEmpty();
    }

    @Test
    void shouldGetListOfUsersByIds() {
        User user1 = createTestUser();
        User user2 = createTestUser2();
        userStorage.save(user1);
        userStorage.save(user2);

        List<User> users = userStorage.getListOfUsers(List.of(user1.getId(), user2.getId()));

        assertThat(users).hasSize(2);
    }

    private User createTestUser() {
        return User.builder()
                .email("test@mail.ru")
                .login("testLogin")
                .name("Test Name")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
    }

    private User createTestUser2() {
        return User.builder()
                .email("test2@mail.ru")
                .login("testLogin2")
                .name("Test Name 2")
                .birthday(LocalDate.of(1995, 5, 5))
                .build();
    }
}
