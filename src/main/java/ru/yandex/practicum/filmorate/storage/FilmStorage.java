package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {
    void save(Film film);

    List<Film> getAll();

    boolean hasFilmWithId(long id);
}
