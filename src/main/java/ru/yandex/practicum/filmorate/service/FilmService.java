package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.db.GenreDbStorage;

import java.time.LocalDate;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {

    private static final LocalDate EARLIEST_RELEASE_DATE = LocalDate.of(1895, 12, 28);
    private final FilmStorage filmStorage;
    private final ValidationService validationService;
    private final GenreDbStorage genreDbStorage;

    public List<Film> getFilms() {
        List<Film> films = filmStorage.getAll();

        if (films.isEmpty()) {
            return Collections.emptyList();
        }
        enrichFilmsWithGenres(films);

        return films;
    }

    public List<Film> getMostPopularFilms(int count) {
        if (count <= 0) {
            log.warn("Запрошено некорректное количество фильмов: {}", count);
            throw new ValidationException("Количество фильмов должно быть положительным");
        }
        List<Film> films = filmStorage.getMostPopular(count);
        enrichFilmsWithGenres(films);
        return films;
    }

    public Film getFilm(long id) {
        Film film = validationService.getFilmOrThrow(id);
        enrichOneFilmWithGenres(film);
        return film;
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

            List<Integer> genresId = genresList.stream()
                    .map(Genre::getId)
                    .toList();

            List<Genre> genres = genreDbStorage.getGenresByIds(genresId);

            if (genres.size() != genresId.size()) {
                log.warn("Запрошены несуществующие жанры");
                throw new NotFoundException("Часть жанров не нашлась");
            }
        }
    }

    private void enrichFilmsWithGenres(List<Film> films) {
        if (films.isEmpty()) {
            return;
        }

        List<Long> filmsIds = films.stream()
                .map(Film::getId)
                .toList();

        Map<Long, List<Genre>> map = genreDbStorage.getGenresByFilmIds(filmsIds);

        for (Film film : films) {
            List<Genre> genres = map.getOrDefault(film.getId(), new ArrayList<>());
            film.setGenres(genres);
        }
    }

    private void enrichOneFilmWithGenres(Film film) {
        enrichFilmsWithGenres(List.of(film));
    }
}
