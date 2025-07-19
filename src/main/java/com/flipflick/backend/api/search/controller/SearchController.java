package com.flipflick.backend.api.search.controller;

import com.flipflick.backend.api.search.dto.*;
import com.flipflick.backend.api.search.service.SearchService;
import com.flipflick.backend.common.config.security.SecurityMember;
import com.flipflick.backend.common.response.ApiResponse;
import com.flipflick.backend.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/search")
@Tag(name="Search", description = "검색 관련 API 입니다.")
public class SearchController {

    private final SearchService searchService;

    @Operation(summary = "영화 검색 API", description = "키워드, 페이지를 받아 영화 리스트를 조회합니다 <br> page는 1 이상")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "영화 리스트 조회 성공")
    })
    @PostMapping("/movie")
    public ResponseEntity<ApiResponse<MovieListPageResponseDTO>> searchMovies(@RequestBody SearchRequestDTO searchRequestDTO) {

        MovieListPageResponseDTO movieListPageResponseDTO = searchService.searchMovieList(searchRequestDTO);
        return ApiResponse.success(SuccessStatus.SEND_MOVIE_LIST_SUCCESS, movieListPageResponseDTO);
    }

    @Operation(summary = "배우 검색 API", description = "키워드, 페이지를 받아 배우 리스트를 조회합니다 <br> page는 1 이상")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "배우 리스트 조회 성공")
    })
    @PostMapping("/cast")
    public ResponseEntity<ApiResponse<CastListPageResponseDTO>> searchCasts(@RequestBody SearchRequestDTO searchRequestDTO) {

        CastListPageResponseDTO castListPageResponseDTO = searchService.searchCastList(searchRequestDTO);
        return ApiResponse.success(SuccessStatus.SEND_CAST_LIST_SUCCESS, castListPageResponseDTO);
    }

    @Operation(summary = "플레이리스트 검색 API", description = "키워드, 페이지를 받아 플레이리스트를 조회합니다 <br> page는 1 이상")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "플레이리스트 조회 성공")
    })
    @PostMapping("/playlist")
    public ResponseEntity<ApiResponse<PlayListPageResponseDTO>> searchPlaylist(@RequestBody SearchRequestDTO searchRequestDTO) {

        PlayListPageResponseDTO playListPageResponseDTO = searchService.searchPlaylist(searchRequestDTO);
        return ApiResponse.success(SuccessStatus.SEND_PLAYLIST_LIST_SUCCESS, playListPageResponseDTO);
    }

    @Operation(summary = "사용자 검색 API", description = "키워드, 페이지를 받아 사용자 리스트를 조회합니다 <br> page는 1 이상")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "플레이리스트 조회 성공")
    })
    @PostMapping("/member")
    public ResponseEntity<ApiResponse<MemberListPageResponseDTO>> searchMember(@RequestBody SearchRequestDTO searchRequestDTO, @AuthenticationPrincipal SecurityMember securityMember) {

        Long userId = (securityMember != null ? securityMember.getId() : null);
        MemberListPageResponseDTO memberListPageResponseDTO = searchService.searchMember(searchRequestDTO, userId);
        return ApiResponse.success(SuccessStatus.SEND_MEMBER_LIST_SUCCESS, memberListPageResponseDTO);
    }

}
