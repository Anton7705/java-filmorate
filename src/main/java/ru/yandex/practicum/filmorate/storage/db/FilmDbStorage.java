package ru.yandex.practicum.filmorate.storage.db;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.db.rowmapper.GenreRowMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class FilmDbStorage extends BaseRepository<Film> implements FilmStorage {
    private static final String FIND_ALL_FILMS_QUERY =
            "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.id AS mpa_id, m.name AS mpa_name " +
                    "FROM films f LEFT JOIN mpa m ON f.mpa_id = m.id";
    private static final String UPDATE_FILM_QUERY =
            "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";
    private static final String FIND_FILM_BY_ID_QUERY =
            "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.id AS mpa_id, m.name AS mpa_name " +
                    "FROM films f LEFT JOIN mpa m ON f.mpa_id = m.id WHERE f.id = ?";
    private static final String INSERT_FILM_QUERY =
            "INSERT INTO films(name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
    private static final String INSERT_LIKE_QUERY = "INSERT INTO likes(film_id, user_id)" +
            "VALUES (?, ?)";
    private static final String DELETE_LIKE_QUERY = "DELETE FROM likes where film_id = ? AND user_id = ?";
    private static final String GET_POPULAR_QUERY =
            "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, " +
                    "m.id AS mpa_id, m.name AS mpa_name, " +
                    "COUNT(l.user_id) AS likes_count " +
                    "FROM films f " +
                    "LEFT JOIN mpa m ON f.mpa_id = m.id " +
                    "LEFT JOIN likes l ON f.id = l.film_id " +
                    "GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.id, m.name " +
                    "ORDER BY likes_count DESC " +
                    "LIMIT ?";
    private static final String DELETE_FILM_GENRES_QUERY =
            "DELETE FROM film_genres WHERE film_id = ?";

    private static final String INSERT_FILM_GENRE_QUERY =
            "INSERT INTO film_genres(film_id, genre_id) VALUES (?, ?)";

    private static final String FIND_GENRES_BY_FILM_ID_QUERY =
            "SELECT g.id, g.name FROM film_genres fg " +
                    "JOIN genres g ON fg.genre_id = g.id " +
                    "WHERE fg.film_id = ? ORDER BY g.id";

    public FilmDbStorage(JdbcTemplate jdbc, RowMapper<Film> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public void save(Film film) {
        if (film.getId() == 0) {
            long id = insert(INSERT_FILM_QUERY,
                    film.getName(),
                    film.getDescription(),
                    film.getReleaseDate(),
                    film.getDuration(),
                    film.getMpa().getId()
            );
            film.setId(id);
        } else {
            update(UPDATE_FILM_QUERY,
                    film.getName(),
                    film.getDescription(),
                    film.getReleaseDate(),
                    film.getDuration(),
                    film.getMpa().getId(),
                    film.getId()
            );
        }

        saveGenres(film);
    }

    private void saveGenres(Film film) {
        jdbc.update(DELETE_FILM_GENRES_QUERY, film.getId());

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            List<Object[]> batchArgs = new ArrayList<>();
            for (Genre genre : film.getGenres()) {
                batchArgs.add(new Object[]{film.getId(), genre.getId()});
            }
            jdbc.batchUpdate(INSERT_FILM_GENRE_QUERY, batchArgs);
        }
    }

    @Override
    public List<Film> getAll() {
        List<Film> films = findMany(FIND_ALL_FILMS_QUERY);
        for (Film film : films) {
            List<Genre> genres = loadGenres(film.getId());
            film.setGenres(genres);
        }
        return films;
    }

    @Override
    public Optional<Film> findById(long id) {
        Optional<Film> filmOpt = findOne(FIND_FILM_BY_ID_QUERY, id);
        filmOpt.ifPresent(film -> {
            List<Genre> genres = loadGenres(film.getId());
            film.setGenres(genres);
        });
        return filmOpt;
    }

    @Override
    public List<Film> getMostPopular(int count) {
        List<Film> films = findMany(GET_POPULAR_QUERY, count);
        for (Film film : films) {
            List<Genre> genres = loadGenres(film.getId());
            film.setGenres(genres);
        }
        return films;
    }

    @Override
    public void removeLike(Film film, long userId) {
        jdbc.update(DELETE_LIKE_QUERY, film.getId(), userId);
    }

    @Override
    public void addLike(Film film, long userId) {
        String checkSql = "SELECT COUNT(*) FROM likes WHERE film_id = ? AND user_id = ?";
        Integer count = jdbc.queryForObject(checkSql, Integer.class, film.getId(), userId);

        if (count == 0) {
            jdbc.update(INSERT_LIKE_QUERY, film.getId(), userId);
        }
    }

    private List<Genre> loadGenres(long filmId) {
        if (filmId == 0) {
            return new ArrayList<>();
        }
        return jdbc.query(FIND_GENRES_BY_FILM_ID_QUERY,
                new GenreRowMapper(), filmId);
    }
}
