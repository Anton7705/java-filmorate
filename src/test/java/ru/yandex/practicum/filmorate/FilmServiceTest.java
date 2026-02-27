package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.ValidationService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class FilmServiceTest {

    private static FilmService filmService;
    private static Film correctFilm;
    private static LocalDate releaseDate = LocalDate.of(2024, 9, 10);

    @BeforeEach
    void setup() {
        FilmStorage filmStorage = new InMemoryFilmStorage();
        UserStorage userStorage = new InMemoryUserStorage();
        ValidationService validationService = new ValidationService(userStorage, filmStorage);
        filmService = new FilmService(filmStorage, validationService);
        correctFilm = Film.builder()
                .name("name")
                .description("description")
                .duration(136.0)
                .releaseDate(releaseDate)
                .build();
    }

    @Test
    @DisplayName("Присвоение ID новому фильму")
    void shouldAssignIdWhenCreatingFilm() {
        filmService.create(correctFilm);
        assertEquals(correctFilm.getId(), 1);
    }

    @Test
    @DisplayName("Создание фильма: корректные данные -> фильм сохраняется")
    void shouldSaveFilmWhenDataIsValid() {
        Film created = filmService.create(correctFilm);

        assertTrue(filmService.getFilms().contains(created));
        assertEquals(1, filmService.getFilms().size());
    }

    @Test
    @DisplayName("Обновление: несуществующий id -> NotFoundException")
    void shouldThrowNotFoundExceptionWhenUpdatingNonExistingFilm() {
        Film filmToUpdate = Film.builder()
                .name("name")
                .description("description")
                .duration(100.0)
                .releaseDate(LocalDate.of(1950, 10, 10))
                .build();

        assertThrows(NotFoundException.class,
                () -> filmService.update(999L, filmToUpdate));
    }

    @Test
    @DisplayName("Валидация даты релиза: раньше 28.12.1895 -> исключение")
    void shouldThrowExceptionWhenReleaseDateIsTooEarly() {
        Film filmWithEarlyDate = Film.builder()
                .name("name")
                .description("description")
                .duration(136.0)
                .releaseDate(LocalDate.of(1700, 10, 10))
                .build();

        assertThrows(ValidationException.class,
                () -> filmService.create(filmWithEarlyDate));
    }

    @Test
    @DisplayName("Валидация даты релиза: позже текущей даты -> исключение")
    void shouldThrowExceptionWhenReleaseDateIsInFuture() {
        Film filmWithFutureDate = Film.builder()
                .name("name")
                .description("description")
                .duration(136.0)
                .releaseDate(LocalDate.now().plusDays(1))
                .build();

        assertThrows(ValidationException.class,
                () -> filmService.create(filmWithFutureDate));
    }

    @Test
    @DisplayName("Обновление фильма: данные успешно обновляются")
    void shouldUpdateFilmSuccessfully() {
        filmService.create(correctFilm);
        long id = correctFilm.getId();
        Film filmToUpdate = Film.builder()
                .name("NewName")
                .description("NewDescription")
                .duration(100.0)
                .releaseDate(LocalDate.of(1950, 10, 10))
                .build();
        Film updatedFilm = filmService.update(id, filmToUpdate);
        assertEquals(filmToUpdate.getName(), updatedFilm.getName());
        assertEquals(filmToUpdate.getDescription(), updatedFilm.getDescription());
    }
}