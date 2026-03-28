package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Mpa {
    private int id;

    @NotBlank(message = "название MPAA не может быть пустым")
    private String name;
}
