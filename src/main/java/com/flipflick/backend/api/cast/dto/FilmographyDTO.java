package com.flipflick.backend.api.cast.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class FilmographyDTO {
    private Long tmdbId;
    private String posterImage;
    private String name;
    private LocalDate releaseDate;
}