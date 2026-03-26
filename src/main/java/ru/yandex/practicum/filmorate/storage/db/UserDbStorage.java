package ru.yandex.practicum.filmorate.storage.db;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
public class UserDbStorage extends BaseRepository<User> implements UserStorage {
    private static final String FIND_ALL_USERS_QUERY = "SELECT * FROM users";
    private static final String FIND_USER_BY_ID_QUERY = "SELECT * FROM users WHERE id = ?";
    private static final String FIND_USER_FRIENDS_BY_ID_QUERY =
            "SELECT u.id, u.name, u.email, u.login, u.birthday FROM friends f " +
            "JOIN users u on u.id = f.friend_id " +
            "WHERE user_id = ?";
    private static final String UPDATE_USER_QUERY = "UPDATE users SET name = ?, email = ?, login = ?, birthday = ? WHERE id = ?";
    private static final String INSERT_USER_QUERY = "INSERT INTO users(name, email, login, birthday)" +
            "VALUES (?, ?, ?, ?)";
    private static final String INSERT_FRIEND_QUERY = "INSERT INTO friends(user_id, friend_id)" +
            "VALUES (?, ?)";
    private static final String DELETE_FRIEND_QUERY = "DELETE FROM friends where user_id = ? AND friend_id = ?";


    public UserDbStorage(JdbcTemplate jdbc, RowMapper<User> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public void save(User user) {
        if (user.getId() == 0) {
            long id = insert(
                    INSERT_USER_QUERY,
                    user.getName(),
                    user.getEmail(),
                    user.getLogin(),
                    user.getBirthday()
            );
            user.setId(id);
        } else {
            insert(
                    UPDATE_USER_QUERY,
                    user.getName(),
                    user.getEmail(),
                    user.getLogin(),
                    user.getBirthday(),
                    user.getId()
            );
        }
    }

    @Override
    public List<User> getAll() {
        return findMany(FIND_ALL_USERS_QUERY);
    }

    @Override
    public Optional<User> findById(long id) {
        return findOne(FIND_USER_BY_ID_QUERY, id);
    }

    public Map<Long, User> getUsersMapByIds(List<Long> ids) {
        List<User> users = getListOfUsers(ids);

        return users.stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
    }

    @Override
    public List<User> getAllFriends(User user) {
        return findMany(FIND_USER_FRIENDS_BY_ID_QUERY, user.getId());
    }

    @Override
    public List<User> getListOfUsers(List<Long> list) {
        if (list == null || list.isEmpty()) {
            return List.of();
        }

        String placeholders = String.join(",", list.stream().map(id -> "?").toList());

        String sql = "SELECT * FROM users WHERE id IN (" + placeholders + ")";

        Object[] params = list.toArray();

        List<User> foundUsers = findMany(sql, params);

        if (foundUsers.size() != list.size()) {
            throw new NotFoundException("Часть запрашиваемых пользователей не надены");
        }

        return foundUsers;
    }

    @Override
    public void addFriendship(long userId, long friendId) {
        try {
            jdbc.update(INSERT_FRIEND_QUERY, userId, friendId);
        } catch (DuplicateKeyException e) {
            throw new ValidationException("Пользователи уже друзья");
        }
    }

    @Override
    public void removeFriendship(long userId, long friendId) {
        jdbc.update(DELETE_FRIEND_QUERY, userId, friendId);
    }
}
