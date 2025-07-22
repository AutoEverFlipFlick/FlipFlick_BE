package com.flipflick.backend.api.review.controller;

import com.flipflick.backend.api.review.dto.ReviewRequestDto;
import com.flipflick.backend.api.review.dto.ReviewResponseDto;
import com.flipflick.backend.api.review.service.ReviewService;
import com.flipflick.backend.common.config.security.SecurityMember;
import com.flipflick.backend.common.response.ApiResponse;
import com.flipflick.backend.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/review")
@RequiredArgsConstructor
@Tag(name = "Review", description = "리뷰 API")
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "리뷰 작성", description = "영화에 대한 리뷰를 작성합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "리뷰 작성 성공")
    })
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<ReviewResponseDto.Create>> createReview(
            @AuthenticationPrincipal SecurityMember securityMember,
            @Valid @RequestBody ReviewRequestDto.Create request) {

        ReviewResponseDto.Create result = reviewService.createReview(securityMember.getId(), request);
        return ApiResponse.success(SuccessStatus.SEND_REVIEW_CREATE_SUCCESS, result);
    }

    @Operation(summary = "리뷰 수정", description = "작성한 리뷰를 수정합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "리뷰 수정 성공")
    })
    @PutMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewResponseDto.Update>> updateReview(
            @AuthenticationPrincipal SecurityMember securityMember,
            @Parameter(description = "리뷰 ID", example = "1")
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewRequestDto.Update request) {

        ReviewResponseDto.Update result = reviewService.updateReview(securityMember.getId(), reviewId, request);
        return ApiResponse.success(SuccessStatus.SEND_REVIEW_UPDATE_SUCCESS, result);
    }

    @Operation(summary = "리뷰 삭제", description = "작성한 리뷰를 삭제합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "리뷰 삭제 성공")
    })
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewResponseDto.Delete>> deleteReview(
            @AuthenticationPrincipal SecurityMember securityMember,
            @Parameter(description = "리뷰 ID", example = "1")
            @PathVariable Long reviewId) {

        ReviewResponseDto.Delete result = reviewService.deleteReview(securityMember.getId(), reviewId);
        return ApiResponse.success(SuccessStatus.SEND_REVIEW_DELETE_SUCCESS, result);
    }

    @Operation(summary = "리뷰 목록 조회 (최신순)", description = "특정 영화의 리뷰를 최신순으로 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "리뷰 목록 조회 성공")
    })
    @GetMapping("/movie/{tmdbId}/latest")
    public ResponseEntity<ApiResponse<ReviewResponseDto.PageResponse>> getReviewsByLatest(
            @Parameter(description = "영화 TMDB ID", example = "550")
            @PathVariable Long tmdbId,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size) {

        ReviewResponseDto.PageResponse result = reviewService.getReviewsByLatest(tmdbId, page, size);
        return ApiResponse.success(SuccessStatus.SEND_REVIEW_LIST_SUCCESS, result);
    }

    @Operation(summary = "리뷰 목록 조회 (인기순)", description = "특정 영화의 리뷰를 인기순으로 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "리뷰 목록 조회 성공")
    })
    @GetMapping("/movie/{tmdbId}/popular")
    public ResponseEntity<ApiResponse<ReviewResponseDto.PageResponse>> getReviewsByPopularity(
            @Parameter(description = "영화 TMDB ID", example = "550")
            @PathVariable Long tmdbId,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size) {

        ReviewResponseDto.PageResponse result = reviewService.getReviewsByPopularity(tmdbId, page, size);
        return ApiResponse.success(SuccessStatus.SEND_REVIEW_LIST_SUCCESS, result);
    }

    @Operation(summary = "닉네임으로 리뷰 목록 조회 (최신순)", description = "특정 사용자의 리뷰를 최신순으로 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "리뷰 목록 조회 성공")
    })
    @GetMapping("/user/{nickname}/latest")
    public ResponseEntity<ApiResponse<ReviewResponseDto.PageResponse>> getReviewsByNicknameLatest(
            @Parameter(description = "사용자 닉네임", example = "영화매니아")
            @PathVariable String nickname,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size) {

        ReviewResponseDto.PageResponse result = reviewService.getReviewsByNicknameLatest(nickname, page, size);
        return ApiResponse.success(SuccessStatus.SEND_REVIEW_LIST_SUCCESS, result);
    }

    @Operation(summary = "리뷰 좋아요/싫어요", description = "리뷰에 좋아요 또는 싫어요를 표시합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "좋아요/싫어요 처리 성공")
    })
    @PostMapping("/like-hate")
    public ResponseEntity<ApiResponse<ReviewResponseDto.LikeHate>> toggleLikeHate(
            @AuthenticationPrincipal SecurityMember securityMember,
            @Valid @RequestBody ReviewRequestDto.LikeHate request) {

        ReviewResponseDto.LikeHate result = reviewService.toggleLikeHate(securityMember.getId(), request);
        return ApiResponse.success(SuccessStatus.SEND_REVIEW_LIKE_HATE_SUCCESS, result);
    }

    @Operation(summary = "내 리뷰 조회", description = "특정 영화에 작성한 내 리뷰를 존재 여부와 함께 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/movie/{tmdbId}/my/status")
    public ResponseEntity<ApiResponse<ReviewResponseDto.MyReview>> getMyReviewWithStatus(
            @AuthenticationPrincipal SecurityMember securityMember,
            @Parameter(description = "영화 TMDB ID", example = "550")
            @PathVariable Long tmdbId) {

        ReviewResponseDto.MyReview result = reviewService.getMyReviewWithStatus(securityMember.getId(), tmdbId);
        return ApiResponse.success(SuccessStatus.GET_REVIEW_SUCCESS, result);
    }
}