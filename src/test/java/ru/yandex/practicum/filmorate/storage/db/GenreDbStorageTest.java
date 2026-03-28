package ru.yandex.practicum.filmorate.storage.db;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.db.rowmapper.GenreRowMapper;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Sql(scripts = {"/schema.sql", "/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class GenreDbStorageTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private GenreDbStorage genreStorage;

    @BeforeEach
    void setUp() {
        genreStorage = new GenreDbStorage(jdbcTemplate, new GenreRowMapper(), namedParameterJdbcTemplate);
    }

    @Test
    void shouldGetAllGenres() {
        List<Genre> all = genreStorage.getAll();

        assertThat(all).hasSize(6);
        assertThat(all).extracting(Genre::getId).containsExactly(1, 2, 3, 4, 5, 6);
        assertThat(all).extracting(Genre::getName).contains(
                "Комедия", "Драма", "Мультфильм", "Триллер", "Документальный", "Боевик"
        );
    }

    @Test
    void shouldGetGenreById() {
        Optional<Genre> genre = genreStorage.get(1);

        assertThat(genre).isPresent();
        assertThat(genre.get().getId()).isEqualTo(1);
        assertThat(genre.get().getName()).isEqualTo("Комедия");
    }

    @Test
    void shouldReturnEmptyForNonExistentId() {
        Optional<Genre> genre = genreStorage.get(999);

        assertThat(genre).isEmpty();
    }
}
