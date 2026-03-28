package ru.yandex.practicum.filmorate.storage.db;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.DictionaryStorage;

import java.util.*;

@Repository
@Slf4j
public class GenreDbStorage extends BaseRepository<Genre> implements DictionaryStorage<Genre> {
    private static final String FIND_ALL_QUERY = "SELECT * FROM genres";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM genres WHERE id = ?";
    private static final String FIND_BY_IDS_QUERY = "SELECT * FROM genres WHERE id IN (:ids)";
    private static final String FIND_GENERS_BY_FILMS_IDS_QUERY = "SELECT fg.film_id, g.id, g.name FROM film_genres fg " +
            "JOIN genres g ON fg.genre_id = g.id " +
            "WHERE film_id IN (:ids)";

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public GenreDbStorage(JdbcTemplate jdbc,
                          RowMapper<Genre> mapper,
                          NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        super(jdbc, mapper);
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public List<Genre> getAll() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public Optional<Genre> get(int id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    public List<Genre> getGenresByIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, Object> params = Map.of("ids", ids);
        return namedParameterJdbcTemplate.query(FIND_BY_IDS_QUERY, params, mapper);
    }

    public Map<Long, List<Genre>> getGenresByFilmIds(List<Long> filmIds) {
        if (filmIds == null || filmIds.isEmpty()) {
            return new HashMap<>();
        }

        Map<String, Object> params = Map.of("ids", filmIds);

        List<Map<String, Object>> rows = namedParameterJdbcTemplate.queryForList(
                FIND_GENERS_BY_FILMS_IDS_QUERY,
                params
        );

        Map<Long, List<Genre>> result = new HashMap<>();

        for (Map<String, Object> row : rows) {
            Long filmId = (Long) row.get("film_id");

            Genre genre = Genre.builder()
                    .id((Integer) row.get("id"))
                    .name((String) row.get("name"))
                    .build();
            result.computeIfAbsent(filmId, k -> new ArrayList<>()).add(genre);
        }

        return result;
    }
}
