package com.flipflick.backend.api.movie.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SearchRequestIdDTO {
    private Long tmdbId;
}