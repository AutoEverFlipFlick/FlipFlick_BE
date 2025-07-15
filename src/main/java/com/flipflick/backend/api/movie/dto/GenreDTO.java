package com.flipflick.backend.api.movie.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GenreDTO {
    private Long tmdbId;
    private String genreName;
}