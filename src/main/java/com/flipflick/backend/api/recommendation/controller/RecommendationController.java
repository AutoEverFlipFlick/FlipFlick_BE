package com.flipflick.backend.api.recommendation.controller;

import com.flipflick.backend.api.recommendation.dto.RecommendationDataDto;
import com.flipflick.backend.api.recommendation.dto.SimilarityBatchDto;
import com.flipflick.backend.api.recommendation.service.RecommendationService;
import com.flipflick.backend.api.review.dto.ReviewResponseDto;

import com.flipflick.backend.common.config.security.SecurityMember;
import com.flipflick.backend.common.response.ApiResponse;
import com.flipflick.backend.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recommendation")
@RequiredArgsConstructor
@Tag(name = "Recommendation", description = "추천 시스템 API")
public class RecommendationController {
    
    private final RecommendationService recommendationService;
    
    @Operation(summary = "유사한 성향 사용자 리뷰 조회", description = "나와 비슷한 성향의 사용자들이 작성한 리뷰를 조회합니다.")
    @GetMapping("/similar-reviews/{tmdbId}")
    public ResponseEntity<ApiResponse<ReviewResponseDto.PageResponse>> getSimilarUserReviews(
            @AuthenticationPrincipal SecurityMember securityMember,
            @Parameter(description = "영화 TMDB ID", example = "550")
            @PathVariable Long tmdbId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        ReviewResponseDto.PageResponse result = recommendationService.getSimilarUserReviews(
            securityMember.getId(), page, size, tmdbId);
        
        return ApiResponse.success(SuccessStatus.SEND_REVIEW_LIST_SUCCESS, result);
    }
    
    @Operation(summary = "추천 시스템 데이터 추출 (Python 서버용)", description = "Python 추천 시스템을 위한 데이터를 추출합니다.")
    @GetMapping("/export-data")
    public ResponseEntity<List<RecommendationDataDto>> exportRecommendationData() {
        List<RecommendationDataDto> data = recommendationService.exportRecommendationData();
        return ResponseEntity.ok(data);
    }
    
    @Operation(summary = "유사도 재계산 트리거", description = "Python 서버에 유사도 재계산을 요청합니다.")
    @PostMapping("/recalculate")
    public ResponseEntity<ApiResponse<String>> triggerRecalculation() {
        recommendationService.triggerSimilarityRecalculation();
        return ApiResponse.success(SuccessStatus.SEND_RECOMMENDATION_SUCCESS, "유사도 재계산 요청 완료");
    }
    
    @Operation(summary = "유사도 데이터 저장", description = "Python 서버에서 계산된 유사도 데이터를 저장합니다.")
    @PostMapping("/similarities")
    public ResponseEntity<ApiResponse<String>> saveSimilarities(@RequestBody SimilarityBatchDto batch) {
        recommendationService.saveSimilarities(batch);
        return ApiResponse.success(SuccessStatus.SEND_RECOMMENDATION_SUCCESS, "유사도 데이터 저장 완료");
    }
    
    @Operation(summary = "유사도 데이터 삭제", description = "기존 유사도 데이터를 삭제합니다.")
    @DeleteMapping("/similarities")
    public ResponseEntity<ApiResponse<String>> deleteSimilarities() {
        recommendationService.deleteAllSimilarities();
        return ApiResponse.success(SuccessStatus.SEND_RECOMMENDATION_SUCCESS, "기존 유사도 데이터 삭제 완료");
    }
}