package com.flipflick.backend.api.cast.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SearchRequestIdDTO {
    private Long tmdbId;
}