package ru.yandex.practicum.filmorate.storage.memory;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.*;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();
    private long nextId = 1;

    @Override
    public void save(Film film) {
        if (film.getId() == 0) {
            film.setId(nextId++);
        }
        films.put(film.getId(), film);
    }

    @Override
    public List<Film> getAll() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Optional<Film> findById(long id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public List<Film> getMostPopular(int count) {
        return getAll().stream()
                .sorted(Comparator.comparingInt(Film::likesCount).reversed())
                .limit(count).toList();
    }

    @Override
    public void addLike(Film film, long userId) {
        film.addLike(userId);
    }

    @Override
    public void removeLike(Film film, long userId) {
        film.removeLike(userId);
    }
}
