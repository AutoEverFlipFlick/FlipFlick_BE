package com.flipflick.backend.api.movie.controller;

import com.flipflick.backend.api.movie.dto.*;
import com.flipflick.backend.api.movie.service.MovieService;
import com.flipflick.backend.common.config.security.SecurityMember;
import com.flipflick.backend.common.response.ApiResponse;
import com.flipflick.backend.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/movie")
@Tag(name="Movie", description = "영화 관련 API 입니다.")
public class MovieController {

    private final MovieService movieService;

    @Operation(summary = "영화 상세 조회 API", description = "TMDB ID를 받아 영화 상세 데이터를 반환합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "영화 상세 정보 조회 성공")
    })
    @PostMapping("/view")
    public ResponseEntity<ApiResponse<MovieDetailResponseDTO>> viewMovieDetail(@RequestBody SearchRequestIdDTO searchRequestIdDTO, @AuthenticationPrincipal SecurityMember securityMember) throws Exception {

        Long memberId = securityMember != null ? securityMember.getId() : null;

        MovieDetailResponseDTO movieDetailResponseDTO = movieService.viewMovieDetail(searchRequestIdDTO, memberId);
        return ApiResponse.success(SuccessStatus.SEND_MOVIE_DETAIL_SUCCESS, movieDetailResponseDTO);
    }

    @Operation(summary = "영화 찜 API", description = "원하는 영화를 찜할 수 있습니다, 다시 한번 더 호출하면 찜 해제 됩니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "영화 찜 토글 성공")
    })
    @PostMapping("/bookmark")
    public ResponseEntity<ApiResponse<Void>> movieBookmark(@RequestBody MovieBWLHRequestDTO movieBWLHRequestDTO, @AuthenticationPrincipal SecurityMember securityMember) {

        movieService.movieBookmark(movieBWLHRequestDTO, securityMember.getId());
        return ApiResponse.success_only(SuccessStatus.SEND_MOVIE_BOOKMARK_SUCCESS);
    }

    @Operation(summary = "찜한 영화 목록 조회", description = "쿼리 파라미터 memberId가 있으면 해당 회원의 찜 목록, 없으면 본인의 찜 목록을 페이징 조회합니다.")
    @GetMapping("/bookmark-list")
    public ResponseEntity<ApiResponse<MovieBWLHListResponseDTO>> getMyBookmarks(
            @AuthenticationPrincipal SecurityMember securityMember,
            @RequestParam(value = "memberId", required = false) Long memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        // memberId 파라미터가 없으면, 인증된 본인의 ID 사용
        Long userId = (memberId != null) ? memberId : securityMember.getId();

        MovieBWLHListResponseDTO movieBWLHListResponseDTO = movieService.getBookmarkedMovies(userId, page, size);
        return ApiResponse.success(SuccessStatus.SEND_MOVIE_BOOKMARK_LIST_SUCCESS, movieBWLHListResponseDTO);
    }

    @Operation(summary = "영화 봤어요 토글 API", description = "봤던 영화를 기록할 수 있습니다, 다시 한번 더 호출하면 해제 됩니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "본 영화 토글 성공")
    })
    @PostMapping("/watched")
    public ResponseEntity<ApiResponse<Void>> movieWatched(@RequestBody MovieBWLHRequestDTO movieBWLHRequestDTO, @AuthenticationPrincipal SecurityMember securityMember) {

        movieService.movieWatched(movieBWLHRequestDTO, securityMember.getId());
        return ApiResponse.success_only(SuccessStatus.SEND_MOVIE_WATCHED_SUCCESS);
    }

    @Operation(summary = "본 영화 목록 조회 API", description = "쿼리 파라미터 memberId가 있으면 해당 회원의 본 영화 목록, 없으면 본인의 본 영화 목록을 페이징 조회합니다.")
    @GetMapping("/watched-list")
    public ResponseEntity<ApiResponse<MovieBWLHListResponseDTO>> getMovieWatched(
            @AuthenticationPrincipal SecurityMember securityMember,
            @RequestParam(value = "memberId", required = false) Long memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        // memberId 파라미터가 없으면, 인증된 본인의 ID 사용
        Long userId = (memberId != null) ? memberId : securityMember.getId();

        MovieBWLHListResponseDTO movieBWLHListResponseDTO = movieService.getMovieWatched(userId, page, size);
        return ApiResponse.success(SuccessStatus.SEND_MOVIE_WATCHED_LIST_SUCCESS, movieBWLHListResponseDTO);
    }

    @Operation(summary = "좋아요, 싫어요 토글 API", description = "좋아요, 싫어요 토글 합니다. TYPE : LIKE / HATE")
    @PostMapping("/like-hate")
    public ResponseEntity<ApiResponse<Void>> movieLikeHate(@RequestBody MovieLikeHateRequestDTO movieLikeHateRequestDTO, @AuthenticationPrincipal SecurityMember securityMember){

        movieService.movieLikeHate(securityMember.getId(), movieLikeHateRequestDTO);
        return ApiResponse.success_only(SuccessStatus.SEND_MOVIE_LIKE_HATE_SUCCESS);
    }

    @Operation(summary = "좋아요 한 영화 목록 조회 API", description = "쿼리 파라미터 memberId가 있으면 해당 회원의 본 영화 목록, 없으면 본인의 본 영화 목록을 페이징 조회합니다.")
    @GetMapping("/like-list")
    public ResponseEntity<ApiResponse<MovieBWLHListResponseDTO>> getMovieLike(
            @AuthenticationPrincipal SecurityMember securityMember,
            @RequestParam(value = "memberId", required = false) Long memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        // memberId 파라미터가 없으면, 인증된 본인의 ID 사용
        Long userId = (memberId != null) ? memberId : securityMember.getId();

        MovieBWLHListResponseDTO movieBWLHListResponseDTO = movieService.getMovieLike(userId, page, size);
        return ApiResponse.success(SuccessStatus.SEND_MOVIE_LIKE_LIST_SUCCESS, movieBWLHListResponseDTO);
    }

    @Operation(summary = "Popcorn 점수 TOP 영화 조회", description = "Popcorn 점수 기준 상위 영화 목록을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/top-popcorn")
    public ResponseEntity<ApiResponse<List<MovieBWLHResponseDTO>>> getTopMoviesByPopcornScore(
            @Parameter(description = "조회할 영화 수", example = "10")
            @RequestParam(defaultValue = "10") int limit) {

        List<MovieBWLHResponseDTO> result = movieService.getTopMoviesByPopcornScore(limit);
        return ApiResponse.success(SuccessStatus.GET_MOVIE_SUCCESS, result);
    }

    @Operation(summary = "Popcorn 점수 수동 재계산", description = "관리자용: Popcorn 점수를 수동으로 재계산합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "재계산 완료")
    })
    @PostMapping("/admin/recalculate-popcorn")
    public ResponseEntity<ApiResponse<String>> manualRecalculatePopcornScores() {
        movieService.manualRecalculatePopcornScores();
        return ApiResponse.success(SuccessStatus.SEND_RECOMMENDATION_SUCCESS, "Popcorn 점수 재계산이 완료되었습니다.");
    }

    @Operation(summary = "박스오피스 TOP10 조회 API", description = "박스오피스에서 TOP10영화를 조회하여 반환합니다.")
    @GetMapping("/boxoffice")
    public ResponseEntity<ApiResponse<BoxOfficeResponseDTO>> getBoxOffice(
            @RequestParam @Parameter(description="오늘 날짜(YYYY-MM-DD)") String today) {

        BoxOfficeResponseDTO boxOfficeResponseDTO = movieService.getYesterdayBoxOffice(today);
        return ApiResponse.success(SuccessStatus.SEND_TODAY_MOVIE_SUCCESS, boxOfficeResponseDTO);
    }
}