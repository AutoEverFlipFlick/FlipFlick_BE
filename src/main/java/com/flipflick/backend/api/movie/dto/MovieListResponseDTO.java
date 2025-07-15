package com.flipflick.backend.api.movie.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class MovieListResponseDTO {

    private Long tmdbId;
    private String title;
    private LocalDate releaseDate;
    private String image;
}
