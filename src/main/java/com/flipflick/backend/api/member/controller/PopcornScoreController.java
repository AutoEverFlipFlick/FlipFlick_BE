package com.flipflick.backend.api.member.controller;

import com.flipflick.backend.api.member.dto.PopcornScoreInfo;
import com.flipflick.backend.api.member.repository.DailyExpLogRepository;
import com.flipflick.backend.api.member.repository.MemberRepository;
import com.flipflick.backend.api.member.service.PopcornScoreService;
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


@Slf4j
@RestController
@RequestMapping("/api/v1/popcorn")
@RequiredArgsConstructor
@Tag(name = "PopcornScore", description = "팝콘지수 API")
public class PopcornScoreController {

    private final PopcornScoreService popcornScoreService;

    @Operation(summary = "내 팝콘지수 조회", description = "사용자의 팝콘지수 정보를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "팝콘지수 조회 성공")
    })
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<PopcornScoreInfo>> getMyPopcornScore(
            @AuthenticationPrincipal SecurityMember securityMember) {

        PopcornScoreInfo result = popcornScoreService.getPopcornScoreInfo(securityMember.getId());
        return ApiResponse.success(SuccessStatus.SEND_POPCORN_SCORE_SUCCESS, result);
    }

    @Operation(summary = "사용자 팝콘지수 조회", description = "특정 사용자의 팝콘지수 정보를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "팝콘지수 조회 성공")
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<PopcornScoreInfo>> getUserPopcornScore(
            @Parameter(description = "사용자 ID", example = "1")
            @PathVariable Long userId) {

        PopcornScoreInfo result = popcornScoreService.getPopcornScoreInfo(userId);
        return ApiResponse.success(SuccessStatus.SEND_POPCORN_SCORE_SUCCESS, result);
    }

    @Operation(summary = "팝콘지수 수동 재계산", description = "관리자용 팝콘지수 수동 재계산 기능입니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "팝콘지수 재계산 성공")
    })
    @PostMapping("/recalculate/{userId}")
    public ResponseEntity<ApiResponse<String>> recalculatePopcornScore(
            @Parameter(description = "사용자 ID", example = "1")
            @PathVariable Long userId) {

        popcornScoreService.recalculatePopcornScore(userId);
        return ApiResponse.success(SuccessStatus.SEND_POPCORN_SCORE_RECALCULATE_SUCCESS, "팝콘지수 재계산 완료");
    }


}