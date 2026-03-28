package ru.yandex.practicum.filmorate.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/genres")
@Slf4j
public class GenreController {

    private final GenreService genreService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Genre> getGenres() {
        log.info("Запрос жанра");
        return genreService.getAll();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Genre getGenrе(@PathVariable int id) {
        log.info("Запрос жанра с id = {}", id);
        return genreService.getGenre(id);
    }
}
