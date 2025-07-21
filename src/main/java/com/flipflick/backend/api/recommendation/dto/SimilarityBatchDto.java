package com.flipflick.backend.api.recommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimilarityBatchDto {
    private Long memberId;
    private List<SimilarityDto> similarities;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimilarityDto {
        private Long similarUserId;
        private Double similarityScore;
    }
}