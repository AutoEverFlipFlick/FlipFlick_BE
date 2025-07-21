package com.flipflick.backend.api.recommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationDataDto {
    private Long memberId;
    private Long movieId;
    private Double rating;
    private List<String> genres;
    private Long tmdbId;
    private String movieTitle;
}