package com.flipflick.backend.api.movie.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BoxOfficeMovieDTO {
    private Long tmdbId;
    private String posterUrl;
    private String title;
    private int rank;
}