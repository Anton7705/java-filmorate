package ru.yandex.practicum.filmorate.storage.db;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.db.rowmapper.MpaRowMapper;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Sql(scripts = {"/schema.sql", "/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class MpaDbStorageTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private MpaDbStorage mpaStorage;

    @BeforeEach
    void setUp() {
        mpaStorage = new MpaDbStorage(jdbcTemplate, new MpaRowMapper());
    }

    @Test
    void shouldGetAllMpa() {
        List<Mpa> all = mpaStorage.getAll();

        assertThat(all).hasSize(5);
        assertThat(all).extracting(Mpa::getId).containsExactly(1, 2, 3, 4, 5);
    }

    @Test
    void shouldGetMpaById() {
        Optional<Mpa> mpa = mpaStorage.get(1);

        assertThat(mpa).isPresent();
        assertThat(mpa.get().getId()).isEqualTo(1);
        assertThat(mpa.get().getName()).isIn("G", "0+");
    }

    @Test
    void shouldReturnEmptyForNonExistentId() {
        Optional<Mpa> mpa = mpaStorage.get(999);

        assertThat(mpa).isEmpty();
    }
}
