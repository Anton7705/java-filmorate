package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class FilmServiceTest {

    private static FilmService filmService;
    private static Film correctFilm;
    private static LocalDate releaseDate = LocalDate.of(2024, 9, 10);

    @BeforeEach
    void setup() {
        filmService = new FilmService();
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
    @DisplayName("Валидация названия: пустое, null или отсутствует -> исключение")
    void shouldThrowExceptionWhenNameIsInvalid() {
        Film filmWithoutName = Film.builder()
                .description("description")
                .duration(136.0)
                .releaseDate(releaseDate)
                .build();
        Film filmWithBlancName = Film.builder()
                .name("")
                .description("description")
                .duration(136.0)
                .releaseDate(releaseDate)
                .build();
        Film filmWithEmptyName = Film.builder()
                .name(null)
                .description("description")
                .duration(136.0)
                .releaseDate(releaseDate)
                .build();
        assertThrows(ValidationException.class, () -> filmService.create(filmWithoutName));
        assertThrows(ValidationException.class, () -> filmService.create(filmWithBlancName));
        assertThrows(ValidationException.class, () -> filmService.create(filmWithEmptyName));
    }

    @Test
    @DisplayName("Валидация описания: отсутствует, null или длина > 200 -> исключение")
    void shouldThrowExceptionWhenDescriptionIsInvalid() {
        Film filmWithoutDescription = Film.builder()
                .name("name")
                .duration(136.0)
                .releaseDate(releaseDate)
                .build();
        Film filmWithEmptyDescription = Film.builder()
                .description(null)
                .name("name")
                .duration(136.0)
                .releaseDate(releaseDate)
                .build();
        String longDescription = "a".repeat(201);
        Film filmWithBigDescription = Film.builder()
                .description(longDescription)
                .name("name")
                .duration(136.0)
                .releaseDate(releaseDate)
                .build();


        assertThrows(ValidationException.class, () -> filmService.create(filmWithoutDescription));
        assertThrows(ValidationException.class, () -> filmService.create(filmWithEmptyDescription));
        assertThrows(ValidationException.class, () -> filmService.create(filmWithBigDescription));
    }

    @Test
    @DisplayName("Валидация продолжительности: отсутствует или отрицательная -> исключение")
    void shouldThrowExceptionWhenDurationIsInvalid() {
        Film filmWithoutDuration = Film.builder()
                .name("name")
                .description("description")
                .releaseDate(releaseDate)
                .build();
        Film filmWithNegativeDuration = Film.builder()
                .name("name")
                .description("description")
                .duration(-10)
                .releaseDate(releaseDate)
                .build();

        assertThrows(ValidationException.class, () -> filmService.create(filmWithoutDuration));
        assertThrows(ValidationException.class, () -> filmService.create(filmWithNegativeDuration));
    }

    @Test
    @DisplayName("Валидация даты релиза: отсутствует или раньше 28.12.1895 -> исключение")
    void shouldThrowExceptionWhenReleaseDateIsInvalid() {
        Film filmWithoutReleaseDate = Film.builder()
                .name("name")
                .description("description")
                .duration(136.0)
                .build();
        Film filmWithUncorrectReleaseDate = Film.builder()
                .name("name")
                .description("description")
                .duration(136.0)
                .releaseDate(LocalDate.of(1700, 10, 10))
                .build();

        assertThrows(ValidationException.class, () -> filmService.create(filmWithoutReleaseDate));
        assertThrows(ValidationException.class, () -> filmService.create(filmWithUncorrectReleaseDate));
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