package com.flipflick.backend.api.movie.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MoviePopcornResponseDTO {

    private Long tmdbId;
    private String posterUrl;
    private String title;
    private int year;
}