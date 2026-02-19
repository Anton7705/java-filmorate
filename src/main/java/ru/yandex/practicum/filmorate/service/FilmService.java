package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class FilmService {

    private static final LocalDate EARLIEST_RELEASE_DATE = LocalDate.of(1895, 12, 28);
    private static final int MAX_DESCRIPTION_LENGTH = 200;
    private Map<Long, Film> films = new HashMap<>();
    private long count = 1;

    public List<Film> getFilms() {
        return new ArrayList<>(films.values());
    }

    public Film create(Film film) {
        validateFilm(film);

        film.setId(count++);
        films.put(film.getId(), film);
        log.debug("Фильм успешно добавлен в список");
        return film;
    }

    public Film update(long id, Film updatedFilm) {
        if (!films.containsKey(id)) {
            throw new ValidationException("Отсутствует фильм с id=" + id);
        }

        validateFilm(updatedFilm);
        updatedFilm.setId(id);
        films.put(id, updatedFilm);
        log.debug("Фильм успешно обновлен");
        return updatedFilm;
    }

    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Пустое название фильма");
            throw new ValidationException("Название не может быть пустым");
        }
        if (film.getDescription() == null) {
            log.warn("Отсутсвует описание фильма");
            throw new ValidationException("Описание не может быть пустым");
        }
        if (film.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
            log.warn("Слишком длинное описание фильма");
            throw new ValidationException("Описание не может быть длиннее 200 символов");
        }

        if (film.getReleaseDate() == null) {
            log.warn("Отсутсвует дата релиза фильма");
            throw new ValidationException("Дата релиза не может быть пуста");
        }
        if (film.getReleaseDate().isBefore(EARLIEST_RELEASE_DATE)) {
            log.warn("Некорректная дата релиза {}", film.getReleaseDate());
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        if (film.getDuration() <= 0) {
            log.warn("Некорректная продолжительность фильма: {}", film.getDuration());
            throw new ValidationException("Продолжительность должна быть положительной");
        }
    }
}
