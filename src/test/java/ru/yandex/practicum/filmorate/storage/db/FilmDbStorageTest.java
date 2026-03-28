package ru.yandex.practicum.filmorate.storage.db;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.db.rowmapper.FilmRowMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Sql(scripts = {"/schema.sql", "/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class FilmDbStorageTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private FilmDbStorage filmStorage;

    @BeforeEach
    void setUp() {
        filmStorage = new FilmDbStorage(jdbcTemplate, new FilmRowMapper());
    }

    @Test
    void shouldSaveAndFindFilm() {
        Film film = createTestFilm();
        filmStorage.save(film);

        Optional<Film> found = filmStorage.findById(film.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo(film.getName());
        assertThat(found.get().getMpa().getId()).isEqualTo(film.getMpa().getId());
    }

    @Test
    void shouldFindAllFilms() {
        filmStorage.save(createTestFilm());
        filmStorage.save(createTestFilm2());

        List<Film> films = filmStorage.getAll();

        assertThat(films).hasSize(2);
    }

    @Test
    void shouldUpdateFilm() {
        Film film = createTestFilm();
        filmStorage.save(film);

        film.setName("Updated Film");
        filmStorage.save(film);

        Optional<Film> updated = filmStorage.findById(film.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getName()).isEqualTo("Updated Film");
    }

    @Test
    void shouldAddAndRemoveLike() {
        Film film = createTestFilm();
        filmStorage.save(film);

        jdbcTemplate.update(
                "INSERT INTO users (id, email, login, name, birthday) VALUES (?, ?, ?, ?, ?)",
                1L, "test@mail.ru", "testuser", "Test User", LocalDate.of(1990, 1, 1)
        );

        filmStorage.addLike(film, 1L);

        List<Film> popular = filmStorage.getMostPopular(10);
        assertThat(popular).hasSize(1);
        assertThat(popular.get(0).getId()).isEqualTo(film.getId());

        filmStorage.removeLike(film, 1L);

        popular = filmStorage.getMostPopular(10);
    }

    private Film createTestFilm() {
        Mpa mpa = Mpa.builder().id(1).name("G").build();
        return Film.builder()
                .name("Test Film")
                .description("Test Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(mpa)
                .build();
    }

    private Film createTestFilm2() {
        Mpa mpa = Mpa.builder().id(2).name("PG").build();
        return Film.builder()
                .name("Test Film 2")
                .description("Test Description 2")
                .releaseDate(LocalDate.of(2005, 5, 5))
                .duration(150)
                .mpa(mpa)
                .build();
    }
}
