package com.flipflick.backend.api.movie.controller;

import com.flipflick.backend.api.movie.dto.MovieDetailResponseDTO;
import com.flipflick.backend.api.movie.dto.SearchRequestIdDTO;
import com.flipflick.backend.api.movie.service.MovieService;
import com.flipflick.backend.common.response.ApiResponse;
import com.flipflick.backend.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<ApiResponse<MovieDetailResponseDTO>> viewMovieDetail(@RequestBody SearchRequestIdDTO searchRequestIdDTO) throws Exception {

        MovieDetailResponseDTO movieDetailResponseDTO = movieService.viewMovieDetail(searchRequestIdDTO);
        return ApiResponse.success(SuccessStatus.SEND_MOVIE_DETAIL_SUCCESS, movieDetailResponseDTO);
    }

}