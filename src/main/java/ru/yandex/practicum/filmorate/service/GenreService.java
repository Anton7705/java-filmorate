package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.DictionaryStorage;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class GenreService {

    private final DictionaryStorage<Genre> genreStorage;
    private final ValidationService validationService;

    public List<Genre> getAll() {
        return genreStorage.getAll();
    }

    public Genre getGenre(int id) {
        return validationService.validateGenre(id);
    }
}
