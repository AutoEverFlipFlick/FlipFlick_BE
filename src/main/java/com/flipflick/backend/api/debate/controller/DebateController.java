package com.flipflick.backend.api.debate.controller;

import com.flipflick.backend.api.debate.dto.DebateRequestDto;
import com.flipflick.backend.api.debate.dto.DebateResponseDto;
import com.flipflick.backend.api.debate.service.DebateService;
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
@RequestMapping("/api/v1/debate")
@RequiredArgsConstructor
@Tag(name = "Debate", description = "토론 API")
public class DebateController {

    private final DebateService debateService;

    @Operation(summary = "토론 작성", description = "영화에 대한 토론을 작성합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토론 작성 성공")
    })
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<DebateResponseDto.DebateCreate>> createDebate(
            @AuthenticationPrincipal SecurityMember securityMember,
            @Valid @RequestBody DebateRequestDto.DebateCreate request) {

        DebateResponseDto.DebateCreate result = debateService.createDebate(securityMember.getId(), request);
        return ApiResponse.success(SuccessStatus.SEND_DEBATE_CREATE_SUCCESS, result);
    }

    @Operation(summary = "토론 수정", description = "작성한 토론을 수정합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토론 수정 성공")
    })
    @PutMapping("/{debateId}")
    public ResponseEntity<ApiResponse<DebateResponseDto.DebateUpdate>> updateDebate(
            @AuthenticationPrincipal SecurityMember securityMember,
            @Parameter(description = "토론 ID", example = "1")
            @PathVariable Long debateId,
            @Valid @RequestBody DebateRequestDto.DebateUpdate request) {

        DebateResponseDto.DebateUpdate result = debateService.updateDebate(securityMember.getId(), debateId, request);
        return ApiResponse.success(SuccessStatus.SEND_DEBATE_UPDATE_SUCCESS, result);
    }

    @Operation(summary = "토론 삭제", description = "작성한 토론을 삭제합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토론 삭제 성공")
    })
    @DeleteMapping("/{debateId}")
    public ResponseEntity<ApiResponse<DebateResponseDto.DebateDelete>> deleteDebate(
            @AuthenticationPrincipal SecurityMember securityMember,
            @Parameter(description = "토론 ID", example = "1")
            @PathVariable Long debateId) {

        DebateResponseDto.DebateDelete result = debateService.deleteDebate(securityMember.getId(), debateId);
        return ApiResponse.success(SuccessStatus.SEND_DEBATE_DELETE_SUCCESS, result);
    }

    @Operation(summary = "토론 목록 조회 (최신순)", description = "특정 영화의 토론을 최신순으로 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토론 목록 조회 성공")
    })
    @GetMapping("/movie/{tmdbId}/latest")
    public ResponseEntity<ApiResponse<DebateResponseDto.DebatePageResponse>> getDebatesByLatest(
            @Parameter(description = "영화 TMDB ID", example = "550")
            @PathVariable Long tmdbId,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size) {

        DebateResponseDto.DebatePageResponse result = debateService.getDebatesByLatest(tmdbId, page, size);
        return ApiResponse.success(SuccessStatus.SEND_DEBATE_LIST_SUCCESS, result);
    }

    @Operation(summary = "토론 목록 조회 (인기순)", description = "특정 영화의 토론을 인기순으로 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토론 목록 조회 성공")
    })
    @GetMapping("/movie/{tmdbId}/popular")
    public ResponseEntity<ApiResponse<DebateResponseDto.DebatePageResponse>> getDebatesByPopularity(
            @Parameter(description = "영화 TMDB ID", example = "550")
            @PathVariable Long tmdbId,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size) {

        DebateResponseDto.DebatePageResponse result = debateService.getDebatesByPopularity(tmdbId, page, size);
        return ApiResponse.success(SuccessStatus.SEND_DEBATE_LIST_SUCCESS, result);
    }

    @Operation(summary = "닉네임으로 토론 목록 조회 (최신순)", description = "특정 사용자의 토론을 닉네임과 최신순으로 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토론 목록 조회 성공")
    })
    @GetMapping("/user/{nickname}/latest")
    public ResponseEntity<ApiResponse<DebateResponseDto.DebatePageResponse>> getDebatesByNicknameLatest(
            @Parameter(description = "사용자 닉네임", example = "영화매니아")
            @PathVariable String nickname,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size) {

        DebateResponseDto.DebatePageResponse result = debateService.getDebatesByNicknameLatest(nickname, page, size);
        return ApiResponse.success(SuccessStatus.SEND_DEBATE_LIST_SUCCESS, result);
    }

    @Operation(summary = "닉네임으로 토론 목록 조회 (인기순)", description = "특정 사용자의 토론을 닉네임과 인기순으로 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토론 목록 조회 성공")
    })
    @GetMapping("/user/{nickname}/popular")
    public ResponseEntity<ApiResponse<DebateResponseDto.DebatePageResponse>> getDebatesByNicknamePopularity(
            @Parameter(description = "사용자 닉네임", example = "영화매니아")
            @PathVariable String nickname,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size) {

        DebateResponseDto.DebatePageResponse result = debateService.getDebatesByNicknamePopularity(nickname, page, size);
        return ApiResponse.success(SuccessStatus.SEND_DEBATE_LIST_SUCCESS, result);
    }

    @Operation(summary = "토론 좋아요/싫어요", description = "토론에 좋아요 또는 싫어요를 표시합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "좋아요/싫어요 처리 성공")
    })
    @PostMapping("/like-hate")
    public ResponseEntity<ApiResponse<DebateResponseDto.DebateLikeHate>> toggleLikeHate(
            @AuthenticationPrincipal SecurityMember securityMember,
            @Valid @RequestBody DebateRequestDto.DebateLikeHate request) {

        DebateResponseDto.DebateLikeHate result = debateService.toggleLikeHate(securityMember.getId(), request);
        return ApiResponse.success(SuccessStatus.SEND_DEBATE_LIKE_HATE_SUCCESS, result);
    }

    @Operation(summary = "토론 상세 조회", description = "토론의 상세 정보를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토론 상세 조회 성공")
    })
    @GetMapping("/{debateId}")
    public ResponseEntity<ApiResponse<DebateResponseDto.DebateDetail>> getDebateDetail(
            @Parameter(description = "토론 ID", example = "1")
            @PathVariable Long debateId) {

        DebateResponseDto.DebateDetail result = debateService.getDebateById(debateId);
        return ApiResponse.success(SuccessStatus.SEND_DEBATE_DETAIL_SUCCESS, result);
    }

    @Operation(summary = "사용자 토론 목록 조회", description = "특정 사용자의 토론 목록을 최신순으로 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토론 목록 조회 성공")
    })
    @GetMapping("/user/debates")
    public ResponseEntity<ApiResponse<DebateResponseDto.DebatePageResponse>> getUserDebates(
            @Parameter(description = "사용자 ID", example = "1")
            @AuthenticationPrincipal SecurityMember securityMember,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        Long memberId = securityMember.getId();
        DebateResponseDto.DebatePageResponse result = debateService.getDebatesByMemberLatest(memberId, page, size);
        return ApiResponse.success(SuccessStatus.SEND_DEBATE_LIST_SUCCESS, result);
    }

    @Operation(summary = "사용자 토론 목록 조회 (인기순)", description = "특정 사용자의 토론 목록을 인기순으로 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토론 목록 조회 성공")
    })
    @GetMapping("/user/debates/popular")
    public ResponseEntity<ApiResponse<DebateResponseDto.DebatePageResponse>> getUserDebatesByPopularity(
            @Parameter(description = "사용자 ID", example = "1")
            @AuthenticationPrincipal SecurityMember securityMember,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        Long memberId = securityMember.getId();
        DebateResponseDto.DebatePageResponse result = debateService.getDebatesByMemberPopularity(memberId, page, size);
        return ApiResponse.success(SuccessStatus.SEND_DEBATE_LIST_SUCCESS, result);
    }

    @Operation(summary = "사용자 토론 목록 조회 (특정 영화, 최신순)", description = "특정 사용자의 특정 영화에 대한 토론 목록을 최신순으로 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토론 목록 조회 성공")
    })
    @GetMapping("/user/movie/{tmdbId}/latest")
    public ResponseEntity<ApiResponse<DebateResponseDto.DebatePageResponse>> getUserDebatesByMovieLatest(
            @AuthenticationPrincipal SecurityMember securityMember,
            @Parameter(description = "영화 TMDB ID", example = "550")
            @PathVariable Long tmdbId,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size) {

        Long memberId = securityMember.getId();
        DebateResponseDto.DebatePageResponse result = debateService.getDebatesByMemberAndMovieLatest(memberId, tmdbId, page, size);
        return ApiResponse.success(SuccessStatus.SEND_DEBATE_LIST_SUCCESS, result);
    }

    @Operation(summary = "사용자 토론 목록 조회 (특정 영화, 인기순)", description = "특정 사용자의 특정 영화에 대한 토론 목록을 인기순으로 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토론 목록 조회 성공")
    })
    @GetMapping("/user/movie/{tmdbId}/popular")
    public ResponseEntity<ApiResponse<DebateResponseDto.DebatePageResponse>> getUserDebatesByMoviePopularity(
            @AuthenticationPrincipal SecurityMember securityMember,
            @Parameter(description = "영화 TMDB ID", example = "550")
            @PathVariable Long tmdbId,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size) {

        Long memberId = securityMember.getId();
        DebateResponseDto.DebatePageResponse result = debateService.getDebatesByMemberAndMoviePopularity(memberId, tmdbId, page, size);
        return ApiResponse.success(SuccessStatus.SEND_DEBATE_LIST_SUCCESS, result);
    }

    @Operation(summary = "사용자의 토론 반응 상태 조회", description = "현재 사용자가 특정 토론에 대한 좋아요/싫어요 상태를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "반응 상태 조회 성공")
    })
    @GetMapping("/{debateId}/user-reaction")
    public ResponseEntity<ApiResponse<DebateResponseDto.UserReaction>> getUserDebateReaction(
            @Parameter(description = "토론 ID", example = "1")
            @PathVariable Long debateId,
            @AuthenticationPrincipal SecurityMember securityMember) {

        // 비로그인 사용자의 경우
        if (securityMember == null) {
            DebateResponseDto.UserReaction result = DebateResponseDto.UserReaction.builder()
                    .isLiked(false)
                    .isHated(false)
                    .build();
            return ApiResponse.success(SuccessStatus.GET_USER_REACTION_SUCCESS, result);
        }

        Long currentUserId = securityMember.getId();
        DebateResponseDto.UserReaction result = debateService.getUserReaction(debateId, currentUserId);
        return ApiResponse.success(SuccessStatus.GET_USER_REACTION_SUCCESS, result);
    }
}