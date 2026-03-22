package ru.yandex.practicum.filmorate.storage.db;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.DictionaryStorage;

import java.util.List;
import java.util.Optional;

@Repository
public class MpaDbStorage extends BaseRepository<Mpa> implements DictionaryStorage<Mpa> {
    private static final String FIND_ALL_QUERY = "SELECT * FROM mpa";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM mpa WHERE id = ?";

    public MpaDbStorage(JdbcTemplate jdbc, RowMapper<Mpa> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public List<Mpa> getAll() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public Optional<Mpa> get(int id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }
}
