package com.flipflick.backend.api.movie.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MovieBWLHResponseDTO {

    private Long tmdbId;
    private String posterImage;
    private String title;
    private int year;
}
