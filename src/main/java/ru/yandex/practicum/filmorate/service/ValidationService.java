package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.DictionaryStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

@Service
@Slf4j
public class ValidationService {

    private final DictionaryStorage<Mpa> mpaStorage;
    private final DictionaryStorage<Genre> genreStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    public ValidationService(@Qualifier("userDbStorage") UserStorage userStorage,
                             @Qualifier("filmDbStorage") FilmStorage filmStorage,
                             @Qualifier("mpaDbStorage") DictionaryStorage<Mpa> mpaStorage,
                             @Qualifier("genreDbStorage") DictionaryStorage<Genre> genreStorage) {
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
        this.mpaStorage = mpaStorage;
        this.genreStorage = genreStorage;
    }

    public User getUserOrThrow(long userId) {
        return userStorage.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователь с id={} не найден", userId);
                    return new NotFoundException("Отсутствует пользователь с id=" + userId);
                });
    }

    public Film getFilmOrThrow(long filmId) {
        return filmStorage.findById(filmId)
                .orElseThrow(() -> {
                    log.warn("Фильм с id={} не найден", filmId);
                    return new NotFoundException("Отсутствует фильм с id=" + filmId);
                });
    }

    public Mpa validateMpa(int id) {
        return mpaStorage.get(id)
                .orElseThrow(() -> new NotFoundException("MPA с id " + id + " не найден"));
    }

    public Genre validateGenre(int id) {
        return genreStorage.get(id)
                .orElseThrow(() -> new NotFoundException("Жанр с id " + id + " не найден"));
    }
}
