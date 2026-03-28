package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.DictionaryStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MpaService {
    private final DictionaryStorage<Mpa> mpaStorage;
    private final ValidationService validationService;

    public List<Mpa> getMpa() {
        return mpaStorage.getAll();
    }

    public Mpa getMpaById(int id) {
        return validationService.validateMpa(id);
    }
}
