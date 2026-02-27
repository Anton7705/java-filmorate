package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.time.LocalDate;
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

    public void addFriend(User friend) {
        if (this.id == friend.id) {
            log.warn("Попытка добавить самого себя в друзья");
            throw new ValidationException("Нельзя добавить самого себя в друзья");
        }
        if (this.friendsIds.contains(friend.id)) {
            log.warn("Попытка добавить одного и того же пользователя в друзья дважды");
            throw new ValidationException("Пользователи уже друзья");
        }
        friendsIds.add(friend.id);
    }

    public void deleteFriend(User friend) {
        if (!this.friendsIds.contains(friend.id)) {
            log.debug("Попытка удалить несуществующего друга (id={}) - операция проигнорирована", friend.id);
            return;
        }
        friendsIds.remove(friend.id);
    }

    public List<Long> getCommonFriendsIds(User friend) {
        if (this.id == friend.id) {
            log.warn("Попытка запросить общих друзей у самого себя");
            throw new ValidationException("Нельзя запросить общих друзей у самого себя");
        }
        Set<Long> setOfId = new HashSet<>(this.friendsIds);
        setOfId.retainAll(friend.friendsIds);
        return setOfId.stream().toList();
    }

    public List<Long> getFriendsIds() {
        return friendsIds.stream().toList();
    }
}
