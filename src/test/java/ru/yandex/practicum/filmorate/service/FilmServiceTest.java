package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.db.*;
import ru.yandex.practicum.filmorate.storage.db.rowmapper.*;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@Sql(scripts = {"/schema.sql", "/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class FilmServiceTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private FilmService filmService;
    private ValidationService validationService;
    private FilmDbStorage filmStorage;
    private UserDbStorage userStorage;
    private MpaDbStorage mpaStorage;
    private GenreDbStorage genreStorage;

    private Mpa testMpa;
    private User testUser;
    private Film correctFilm;

    @BeforeEach
    void setUp() {
        filmStorage = new FilmDbStorage(jdbcTemplate, new FilmRowMapper());
        userStorage = new UserDbStorage(jdbcTemplate, new UserRowMapper());
        mpaStorage = new MpaDbStorage(jdbcTemplate, new MpaRowMapper());
        genreStorage = new GenreDbStorage(jdbcTemplate, new GenreRowMapper(), namedParameterJdbcTemplate);

        validationService = new ValidationService(userStorage, filmStorage, mpaStorage, genreStorage);
        filmService = new FilmService(filmStorage, validationService, genreStorage);

        testUser = User.builder()
                .email("test@mail.ru")
                .login("testlogin")
                .name("Test User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
        userStorage.save(testUser);

        testMpa = mpaStorage.get(1).orElseThrow();

        correctFilm = Film.builder()
                .name("name")
                .description("description")
                .duration(136.0)
                .releaseDate(LocalDate.of(2024, 9, 10))
                .mpa(testMpa)
                .build();
    }

    @Test
    @DisplayName("Присвоение ID новому фильму")
    void shouldAssignIdWhenCreatingFilm() {
        filmService.create(correctFilm);
        assertThat(correctFilm.getId()).isPositive();
    }

    @Test
    @DisplayName("Создание фильма: корректные данные -> фильм сохраняется")
    void shouldSaveFilmWhenDataIsValid() {
        Film created = filmService.create(correctFilm);

        List<Film> films = filmService.getFilms();

        assertThat(films)
                .hasSize(1)
                .first()
                .satisfies(film -> {
                    assertThat(film.getId()).isEqualTo(created.getId());
                    assertThat(film.getName()).isEqualTo("name");
                    assertThat(film.getGenres()).isEmpty();
                });
    }

    @Test
    @DisplayName("Создание фильма: с жанрами")
    void shouldSaveFilmWithGenres() {
        correctFilm.setGenres(List.of(
                Genre.builder().id(1).build(),
                Genre.builder().id(2).build()
        ));

        Film created = filmService.create(correctFilm);

        Film found = filmService.getFilm(created.getId());
        assertThat(found.getGenres()).hasSize(2);
        assertThat(found.getGenres()).extracting("id").containsExactly(1, 2);
    }

    @Test
    @DisplayName("Обновление: несуществующий id -> NotFoundException")
    void shouldThrowNotFoundExceptionWhenUpdatingNonExistingFilm() {
        Film filmToUpdate = Film.builder()
                .name("name")
                .description("description")
                .duration(100.0)
                .releaseDate(LocalDate.of(1950, 10, 10))
                .mpa(testMpa)
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
                .mpa(testMpa)
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
                .mpa(testMpa)
                .build();

        assertThrows(ValidationException.class,
                () -> filmService.create(filmWithFutureDate));
    }

    @Test
    @DisplayName("Обновление фильма: данные успешно обновляются")
    void shouldUpdateFilmSuccessfully() {
        Film created = filmService.create(correctFilm);
        long id = created.getId();

        Film filmToUpdate = Film.builder()
                .name("NewName")
                .description("NewDescription")
                .duration(100.0)
                .releaseDate(LocalDate.of(1950, 10, 10))
                .mpa(testMpa)
                .build();

        Film updatedFilm = filmService.update(id, filmToUpdate);

        assertThat(updatedFilm.getName()).isEqualTo("NewName");
        assertThat(updatedFilm.getDescription()).isEqualTo("NewDescription");
    }

    @Test
    @DisplayName("Добавление лайка: успешное добавление")
    void shouldAddLikeSuccessfully() {
        Film film = filmService.create(correctFilm);

        filmService.addLikeToFilm(film.getId(), testUser.getId());

        String sql = "SELECT COUNT(*) FROM likes WHERE film_id = ? AND user_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, film.getId(), testUser.getId());
        assertEquals(1, count);
    }

    @Test
    @DisplayName("Добавление лайка: повторный лайк от того же пользователя")
    void shouldNotThrowExceptionWhenAddingDuplicateLike() {
        Film film = filmService.create(correctFilm);

        filmService.addLikeToFilm(film.getId(), testUser.getId());
        assertDoesNotThrow(() -> filmService.addLikeToFilm(film.getId(), testUser.getId()));

        String sql = "SELECT COUNT(*) FROM likes WHERE film_id = ? AND user_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, film.getId(), testUser.getId());
        assertThat(count).isEqualTo(1);
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
        assertThat(updatedFilm.likesCount()).isZero();
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
                .email("user2@mail.ru")
                .login("user2")
                .name("User Two")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
        userStorage.save(user2);
        User user3 = User.builder()
                .email("user3@mail.ru")
                .login("user3")
                .name("User Three")
                .birthday(LocalDate.of(2000, 9, 9))
                .build();
        userStorage.save(user3);

        Film film1 = Film.builder()
                .name("name1")
                .description("Description for 1")
                .duration(100)
                .releaseDate(LocalDate.now())
                .mpa(testMpa)
                .build();
        filmService.create(film1);
        Film film2 = Film.builder()
                .name("name2")
                .description("Description for 2")
                .duration(100)
                .releaseDate(LocalDate.now())
                .mpa(testMpa)
                .build();
        filmService.create(film2);
        Film film3 = Film.builder()
                .name("name3")
                .description("Description for 3")
                .duration(100)
                .releaseDate(LocalDate.now())
                .mpa(testMpa)
                .build();
        filmService.create(film3);

        filmService.addLikeToFilm(film2.getId(), testUser.getId());
        filmService.addLikeToFilm(film2.getId(), user2.getId());
        filmService.addLikeToFilm(film2.getId(), user3.getId());

        filmService.addLikeToFilm(film1.getId(), testUser.getId());
        filmService.addLikeToFilm(film1.getId(), user2.getId());

        filmService.addLikeToFilm(film3.getId(), testUser.getId());

        List<Film> popularFilms = filmService.getMostPopularFilms(10);

        assertThat(popularFilms).hasSize(3);
        assertThat(popularFilms.get(0).getId()).isEqualTo(film2.getId());
        assertThat(popularFilms.get(1).getId()).isEqualTo(film1.getId());
        assertThat(popularFilms.get(2).getId()).isEqualTo(film3.getId());
    }

    @Test
    @DisplayName("Получение популярных фильмов: ограничение количества")
    void shouldRespectCountParameter() {
        for (int i = 1; i <= 5; i++) {
            Film film = Film.builder()
                    .name("name")
                    .description("Description")
                    .duration(100)
                    .releaseDate(LocalDate.now())
                    .mpa(testMpa)
                    .build();
            filmService.create(film);
            filmService.addLikeToFilm(film.getId(), testUser.getId());
        }

        List<Film> popularFilms = filmService.getMostPopularFilms(3);
        assertThat(popularFilms).hasSize(3);
    }
}