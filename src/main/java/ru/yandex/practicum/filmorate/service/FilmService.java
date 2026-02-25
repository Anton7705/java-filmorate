package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {

    private static final LocalDate EARLIEST_RELEASE_DATE = LocalDate.of(1895, 12, 28);
    private final FilmStorage filmStorage;
    private long count = 1;

    public List<Film> getFilms() {
        return filmStorage.getAll();
    }

    public Film create(Film film) {
        validateFilm(film);

        film.setId(count++);
        filmStorage.save(film);
        log.debug("Фильм успешно добавлен в список");
        return film;
    }

    public Film update(long id, Film updatedFilm) {
        if (!filmStorage.hasFilmWithId(id)) {
            log.warn("Фильм с id={} не найден", id);
            throw new NotFoundException("Отсутствует фильм с id=" + id);
        }

        validateFilm(updatedFilm);
        updatedFilm.setId(id);
        filmStorage.save(updatedFilm);
        log.debug("Фильм успешно обновлен");
        return updatedFilm;
    }

    private void validateFilm(Film film) {
        if (film.getReleaseDate().isBefore(EARLIEST_RELEASE_DATE) || film.getReleaseDate().isAfter(LocalDate.now())) {
            log.warn("Некорректная дата релиза {}", film.getReleaseDate());
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года и позже текущей даты");
        }
    }
}
