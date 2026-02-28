package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Builder
@Slf4j
public class User {
    private long id;

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Некорректный формат email")
    private String email;

    @NotBlank(message = "Логин не может быть пустым")
    @Pattern(regexp = "\\S+", message = "Логин не может содержать пробелы")
    private String login;

    private String name;

    @NotNull(message = "Дата рождения обязательна")
    @Past(message = "Дата рождения должна быть в прошлом")
    private LocalDate birthday;

    private final Set<Long> friendsIds = new HashSet<>();

    public List<Long> getFriendsIds() {
        return new ArrayList<>(friendsIds);
    }

    public void addFriendId(long friendId) {
        friendsIds.add(friendId);
    }

    public void removeFriendId(long friendId) {
        friendsIds.remove(friendId);
    }
}
