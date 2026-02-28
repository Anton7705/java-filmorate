package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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
    private final ValidationService validationService;
    private long count = 1;

    public List<Film> getFilms() {
        return filmStorage.getAll();
    }

    public List<Film> getMostPopularFilms(int count) {
        if (count <= 0) {
            log.warn("Запрошено некорректное количество фильмов: {}", count);
            throw new ValidationException("Количество фильмов должно быть положительным");
        }
        return filmStorage.getMostPopular(count);
    }

    public Film getFilm(long id) {
        return validationService.getFilmOrThrow(id);
    }

    public Film create(Film film) {
        validateFilm(film);

        film.setId(count++);
        filmStorage.save(film);
        log.debug("Фильм успешно добавлен в список");
        return film;
    }

    public Film update(long id, Film updatedFilm) {
        validationService.getFilmOrThrow(id);

        validateFilm(updatedFilm);
        updatedFilm.setId(id);
        filmStorage.save(updatedFilm);
        log.debug("Фильм успешно обновлен");
        return updatedFilm;
    }

    public void addLikeToFilm(long id, long userId) {
        validationService.getUserOrThrow(userId);
        Film film = validationService.getFilmOrThrow(id);
        film.addLike(userId);
        filmStorage.save(film);
    }

    public void removeLikeFromFilm(long id, long userId) {
        validationService.getUserOrThrow(userId);
        Film film = validationService.getFilmOrThrow(id);
        film.removeLike(userId);
        filmStorage.save(film);
    }

    private void validateFilm(Film film) {
        if (film.getReleaseDate().isBefore(EARLIEST_RELEASE_DATE) || film.getReleaseDate().isAfter(LocalDate.now())) {
            log.warn("Некорректная дата релиза {}", film.getReleaseDate());
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года и позже текущей даты");
        }
    }
}
