package ru.yandex.practicum.filmorate.storage;

import java.util.List;
import java.util.Optional;

public interface DictionaryStorage<T> {
    List<T> getAll();

    Optional<T> get(int id);
}

