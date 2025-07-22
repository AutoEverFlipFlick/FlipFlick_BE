package com.flipflick.backend.api.movie.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BoxOfficeResponseDTO {
    private List<BoxOfficeMovieDTO> movies;
}
