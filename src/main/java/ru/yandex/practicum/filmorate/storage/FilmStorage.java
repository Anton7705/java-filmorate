package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

public interface FilmStorage {
    void save(Film film);

    List<Film> getAll();

    Optional<Film> findById(long id);

    List<Film> getMostPopular(int count);
}
