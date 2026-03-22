package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.time.LocalDate;
import java.util.*;

@Service
@Slf4j
public class FilmService {
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage, ValidationService validationService) {
        this.filmStorage = filmStorage;
        this.validationService = validationService;
    }

    private static final LocalDate EARLIEST_RELEASE_DATE = LocalDate.of(1895, 12, 28);
    private final FilmStorage filmStorage;
    private final ValidationService validationService;

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
        validationService.validateMpa(film.getMpa().getId());
        validateGenres(film);
        filmStorage.save(film);
        log.debug("Фильм успешно добавлен в список");
        return film;
    }

    public Film update(long id, Film updatedFilm) {
        validationService.getFilmOrThrow(id);
        validationService.validateMpa(updatedFilm.getMpa().getId());
        validateGenres(updatedFilm);
        validateFilm(updatedFilm);
        updatedFilm.setId(id);
        filmStorage.save(updatedFilm);
        log.debug("Фильм успешно обновлен");
        return updatedFilm;
    }

    public void addLikeToFilm(long id, long userId) {
        validationService.getUserOrThrow(userId);
        Film film = validationService.getFilmOrThrow(id);
        filmStorage.addLike(film, userId);
        filmStorage.save(film);
    }

    public void removeLikeFromFilm(long id, long userId) {
        validationService.getUserOrThrow(userId);
        Film film = validationService.getFilmOrThrow(id);
        filmStorage.removeLike(film, id);
        filmStorage.save(film);
    }

    private void validateFilm(Film film) {
        if (film.getReleaseDate().isBefore(EARLIEST_RELEASE_DATE) || film.getReleaseDate().isAfter(LocalDate.now())) {
            log.warn("Некорректная дата релиза {}", film.getReleaseDate());
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года и позже текущей даты");
        }
    }

    private void validateGenres(Film film) {
        if (film.getGenres() != null) {

            Set<Genre> uniqueGenres = new TreeSet<>(Comparator.comparingInt(Genre::getId));
            uniqueGenres.addAll(film.getGenres());

            List<Genre> genresList = new ArrayList<>(uniqueGenres);
            film.setGenres(genresList);

            genresList.stream()
                    .map(Genre::getId)
                    .forEach(validationService::validateGenre);
        }
    }
}
