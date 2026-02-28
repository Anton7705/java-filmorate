package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FilmServiceTest {

    private static FilmService filmService;
    private static Film correctFilm;
    private UserStorage userStorage;
    private User testUser;
    private static LocalDate releaseDate = LocalDate.of(2024, 9, 10);

    @BeforeEach
    void setup() {
        FilmStorage filmStorage = new InMemoryFilmStorage();
        userStorage = new InMemoryUserStorage();
        ValidationService validationService = new ValidationService(userStorage, filmStorage);
        filmService = new FilmService(filmStorage, validationService);
        correctFilm = Film.builder()
                .name("name")
                .description("description")
                .duration(136.0)
                .releaseDate(releaseDate)
                .build();
        testUser = User.builder()
                .id(1L)
                .email("test@mail.ru")
                .login("testlogin")
                .name("Test User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
        userStorage.save(testUser);
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

    @Test
    @DisplayName("Добавление лайка: успешное добавление")
    void shouldAddLikeSuccessfully() {
        Film film = filmService.create(correctFilm);

        filmService.addLikeToFilm(film.getId(), testUser.getId());

        Film updatedFilm = filmService.getFilm(film.getId());
        assertEquals(1, updatedFilm.likesCount());
    }

    @Test
    @DisplayName("Добавление лайка: повторный лайк от того же пользователя")
    void shouldNotThrowExceptionWhenAddingDuplicateLike() {
        Film film = filmService.create(correctFilm);

        filmService.addLikeToFilm(film.getId(), testUser.getId());
        assertDoesNotThrow(() -> filmService.addLikeToFilm(film.getId(), testUser.getId()));

        Film updatedFilm = filmService.getFilm(film.getId());
        assertEquals(1, updatedFilm.likesCount());
    }

    @Test
    @DisplayName("Добавление лайка: несуществующий пользователь -> исключение")
    void shouldThrowExceptionWhenAddingLikeFromNonExistingUser() {
        Film film = filmService.create(correctFilm);

        assertThrows(NotFoundException.class,
                () -> filmService.addLikeToFilm(film.getId(), 999L));
    }

    @Test
    @DisplayName("Удаление лайка: успешное удаление")
    void shouldRemoveLikeSuccessfully() {
        Film film = filmService.create(correctFilm);
        filmService.addLikeToFilm(film.getId(), testUser.getId());

        filmService.removeLikeFromFilm(film.getId(), testUser.getId());

        Film updatedFilm = filmService.getFilm(film.getId());
        assertEquals(0, updatedFilm.likesCount());
    }

    @Test
    @DisplayName("Удаление лайка: удаление несуществующего лайка (не падает)")
    void shouldNotThrowExceptionWhenRemovingNonExistingLike() {
        Film film = filmService.create(correctFilm);

        assertDoesNotThrow(() -> filmService.removeLikeFromFilm(film.getId(), testUser.getId()));
    }

    @Test
    @DisplayName("Получение популярных фильмов: сортировка по количеству лайков")
    void shouldReturnFilmsSortedByLikesCount() {
        User user2 = User.builder()
                .id(2L)
                .email("user2@mail.ru")
                .login("user2")
                .name("User Two")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User user3 = User.builder()
                .id(3L)
                .email("user3@mail.ru")
                .login("user3")
                .name("User Three")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        userStorage.save(user2);
        userStorage.save(user3);

        Film film1 = filmService.create(Film.builder().name("Film1").description("Desc1").duration(100).releaseDate(LocalDate.now()).build());
        Film film2 = filmService.create(Film.builder().name("Film2").description("Desc2").duration(100).releaseDate(LocalDate.now()).build());
        Film film3 = filmService.create(Film.builder().name("Film3").description("Desc3").duration(100).releaseDate(LocalDate.now()).build());

        filmService.addLikeToFilm(film2.getId(), testUser.getId());
        filmService.addLikeToFilm(film2.getId(), user2.getId());
        filmService.addLikeToFilm(film2.getId(), user3.getId());

        filmService.addLikeToFilm(film1.getId(), testUser.getId());
        filmService.addLikeToFilm(film1.getId(), user2.getId());

        filmService.addLikeToFilm(film3.getId(), testUser.getId());

        List<Film> popularFilms = filmService.getMostPopularFilms(10);

        assertEquals(3, popularFilms.size());
        assertEquals(film2.getId(), popularFilms.get(0).getId());
        assertEquals(film1.getId(), popularFilms.get(1).getId());
        assertEquals(film3.getId(), popularFilms.get(2).getId());
    }

    @Test
    @DisplayName("Получение популярных фильмов")
    void shouldRespectCountParameter() {
        for (int i = 1; i <= 5; i++) {
            Film film = filmService.create(Film.builder()
                    .name("Film" + i)
                    .description("Desc" + i)
                    .duration(100)
                    .releaseDate(LocalDate.now())
                    .build());
            filmService.addLikeToFilm(film.getId(), testUser.getId());
        }

        List<Film> popularFilms = filmService.getMostPopularFilms(3);
        assertEquals(3, popularFilms.size());
    }
}