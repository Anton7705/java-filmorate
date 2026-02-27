package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmTest {

    private Film film;

    @BeforeEach
    void setup() {
        film = Film.builder()
                .id(1L)
                .name("Test Film")
                .description("Test Description")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(120)
                .build();
    }

    @Test
    @DisplayName("addLike: успешное добавление лайка")
    void shouldAddLikeSuccessfully() {
        film.addLike(1L);

        assertEquals(1, film.likesCount());
        assertTrue(film.getLikes().contains(1L));
    }

    @Test
    @DisplayName("addLike: повторное добавление лайка от того же пользователя (не ломается)")
    void shouldNotBreakWhenAddingDuplicateLike() {
        film.addLike(1L);
        film.addLike(1L); // Set не добавит дубликат

        assertEquals(1, film.likesCount());
    }

    @Test
    @DisplayName("removeLike: успешное удаление лайка")
    void shouldRemoveLikeSuccessfully() {
        film.addLike(1L);
        film.removeLike(1L);

        assertEquals(0, film.likesCount());
        assertFalse(film.getLikes().contains(1L));
    }

    @Test
    @DisplayName("removeLike: удаление несуществующего лайка (не падает)")
    void shouldNotThrowExceptionWhenRemovingNonExistingLike() {
        assertDoesNotThrow(() -> film.removeLike(999L));
    }

    @Test
    @DisplayName("likesCount: возвращает правильное количество")
    void shouldReturnCorrectLikesCount() {
        assertEquals(0, film.likesCount());

        film.addLike(1L);
        assertEquals(1, film.likesCount());

        film.addLike(2L);
        assertEquals(2, film.likesCount());

        film.removeLike(1L);
        assertEquals(1, film.likesCount());
    }
}
