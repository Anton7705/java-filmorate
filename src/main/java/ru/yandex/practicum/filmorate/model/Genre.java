package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Genre {
    private int id;

    @NotNull
    @NotBlank(message = "Жанр не может быть пустым")
    private String name;
}
