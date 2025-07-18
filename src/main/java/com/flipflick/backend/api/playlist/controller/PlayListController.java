package com.flipflick.backend.api.playlist.controller;

import com.flipflick.backend.api.playlist.dto.PlayListRequestDto;
import com.flipflick.backend.api.playlist.dto.PlayListResponseDto;
import com.flipflick.backend.api.playlist.service.PlayListService;
import com.flipflick.backend.common.config.security.SecurityMember;
import com.flipflick.backend.common.response.ApiResponse;
import com.flipflick.backend.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/playlist")
@RequiredArgsConstructor
@Tag(name = "PlayList", description = "플레이리스트 API")
public class PlayListController {

    private final PlayListService playListService;

    @Operation(summary = "전체 플레이리스트 조회", description = "공개된 전체 플레이리스트를 페이지네이션으로 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "전체 플레이리스트 조회 성공")
    })
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<PlayListResponseDto.PageResponse>> getAllPlayLists(
            @Parameter(description = "정렬 기준 (popularity: 인기순, latest: 최신순, oldest: 오래된순)", example = "popularity")
            @RequestParam(defaultValue = "popularity") String sortBy,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size) {

        PlayListResponseDto.PageResponse result = playListService.getAllPlayLists(sortBy, page, size);
        return ApiResponse.success(SuccessStatus.SEND_PLAYLIST_LIST_SUCCESS, result);
    }

    @Operation(summary = "내가 찜한 플레이리스트 조회", description = "사용자가 북마크한 플레이리스트를 페이지네이션으로 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "찜한 플레이리스트 조회 성공")
    })
    @GetMapping("/bookmarked")
    public ResponseEntity<ApiResponse<PlayListResponseDto.PageResponse>> getBookmarkedPlayLists(
            @AuthenticationPrincipal SecurityMember securityMember,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size) {

        PlayListResponseDto.PageResponse result = playListService.getBookmarkedPlayLists(securityMember.getId(), page, size);
        return ApiResponse.success(SuccessStatus.SEND_PLAYLIST_LIST_SUCCESS, result);
    }

    @Operation(summary = "내가 작성한 플레이리스트 조회", description = "사용자가 작성한 플레이리스트를 페이지네이션으로 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "내 플레이리스트 조회 성공")
    })
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<PlayListResponseDto.PageResponse>> getMyPlayLists(
            @AuthenticationPrincipal SecurityMember securityMember,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size) {

        PlayListResponseDto.PageResponse result = playListService.getMyPlayLists(securityMember.getId(), page, size);
        return ApiResponse.success(SuccessStatus.SEND_PLAYLIST_LIST_SUCCESS, result);
    }

    @Operation(summary = "플레이리스트 상세 조회", description = "특정 플레이리스트의 상세 정보를 페이지네이션으로 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "플레이리스트 상세 조회 성공")
    })
    @GetMapping("/{playListId}")
    public ResponseEntity<ApiResponse<PlayListResponseDto.Detail>> getPlayListDetail(
            @Parameter(description = "플레이리스트 ID", example = "1")
            @PathVariable Long playListId,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size) {

        PlayListResponseDto.Detail result = playListService.getPlayListDetail(playListId, page, size);
        return ApiResponse.success(SuccessStatus.SEND_PLAYLIST_DETAIL_SUCCESS, result);
    }

    @Operation(summary = "플레이리스트 생성", description = "새로운 플레이리스트를 생성합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "플레이리스트 생성 성공")
    })
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<PlayListResponseDto.Create>> createPlayList(
            @AuthenticationPrincipal SecurityMember securityMember,
            @RequestBody PlayListRequestDto.Create request) {

        PlayListResponseDto.Create result = playListService.createPlayList(securityMember.getId(), request);
        return ApiResponse.success(SuccessStatus.SEND_PLAYLIST_CREATE_SUCCESS, result);
    }

    @Operation(summary = "플레이리스트 수정", description = "플레이리스트의 제목, 공개 설정, 영화 목록을 수정합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "플레이리스트 수정 성공")
    })
    @PutMapping("/{playListId}")
    public ResponseEntity<ApiResponse<PlayListResponseDto.Update>> updatePlayList(
            @AuthenticationPrincipal SecurityMember securityMember,
            @Parameter(description = "플레이리스트 ID", example = "1")
            @PathVariable Long playListId,
            @RequestBody PlayListRequestDto.Update request) {

        PlayListResponseDto.Update result = playListService.updatePlayList(securityMember.getId(), playListId, request);
        return ApiResponse.success(SuccessStatus.SEND_PLAYLIST_UPDATE_SUCCESS, result);
    }

    @Operation(summary = "플레이리스트 검색", description = "제목으로 플레이리스트를 검색합니다. 부분 검색이 가능합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "플레이리스트 검색 성공")
    })
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PlayListResponseDto.PageResponse>> searchPlayLists(
            @Parameter(description = "검색 키워드", example = "키워드")
            @RequestParam String keyword,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size) {

        PlayListResponseDto.PageResponse result = playListService.searchPlayLists(keyword, page, size);
        return ApiResponse.success(SuccessStatus.SEND_PLAYLIST_SEARCH_SUCCESS, result);
    }

    @Operation(summary = "플레이리스트 삭제", description = "플레이리스트를 소프트 삭제합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "플레이리스트 삭제 성공")
    })
    @DeleteMapping("/{playListId}")
    public ResponseEntity<ApiResponse<PlayListResponseDto.Delete>> deletePlayList(
            @AuthenticationPrincipal SecurityMember securityMember,
            @Parameter(description = "플레이리스트 ID", example = "1")
            @PathVariable Long playListId){

        PlayListResponseDto.Delete result = playListService.deletePlayList(securityMember.getId(), playListId);
        return ApiResponse.success(SuccessStatus.SEND_PLAYLIST_DELETE_SUCCESS, result);
    }

    @Operation(summary = "플레이리스트 북마크 토글", description = "플레이리스트를 북마크하거나 해제합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "북마크 토글 성공")
    })
    @PostMapping("/bookmark")
    public ResponseEntity<ApiResponse<String>> toggleBookmark(
            @AuthenticationPrincipal SecurityMember securityMember,
            @RequestBody PlayListRequestDto.Bookmark request) {

        boolean isBookmarked = playListService.toggleBookmark(securityMember.getId(), request.getPlayListId());
        String message = isBookmarked ? "북마크가 추가되었습니다." : "북마크가 해제되었습니다.";
        return ApiResponse.success(SuccessStatus.SEND_PLAYLIST_BOOKMARK_SUCCESS, message);
    }

    @Operation(summary = "사용자 북마크 플레이리스트 ID 목록 조회", description = "사용자가 북마크한 모든 플레이리스트의 ID 목록을 반환합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "북마크 플레이리스트 ID 목록 조회 성공")
    })
    @GetMapping("/user/bookmarks")
    public ResponseEntity<ApiResponse<PlayListResponseDto.BookmarkIds>> getBookmarkedPlayListIds(
            @AuthenticationPrincipal SecurityMember securityMember
    ) {

        PlayListResponseDto.BookmarkIds result = playListService.getBookmarkedPlayListIds(securityMember.getId());
        return ApiResponse.success(SuccessStatus.SEND_PLAYLIST_BOOKMARK_LIST_SUCCESS, result);
    }

    @Operation(summary = "닉네임으로 플레이리스트 조회", description = "특정 사용자의 공개 플레이리스트를 최신순으로 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "플레이리스트 조회 성공")
    })
    @GetMapping("/user/{nickname}")
    public ResponseEntity<ApiResponse<PlayListResponseDto.PageResponse>> getPlayListsByNickname(
            @Parameter(description = "사용자 닉네임", example = "영화매니아")
            @PathVariable String nickname,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size) {

        PlayListResponseDto.PageResponse result = playListService.getPlayListsByNickname(nickname, page, size);
        return ApiResponse.success(SuccessStatus.SEND_PLAYLIST_LIST_SUCCESS, result);
    }
}