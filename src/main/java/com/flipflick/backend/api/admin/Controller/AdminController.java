package com.flipflick.backend.api.admin.Controller;

import com.flipflick.backend.api.admin.dto.DashboardStatResponseDto;
import com.flipflick.backend.api.admin.dto.MemberStatusUpdateRequestDto;
import com.flipflick.backend.api.admin.dto.MovieReviewCountResponseDto;
import com.flipflick.backend.api.admin.dto.PopcornGradeResponseDto;
import com.flipflick.backend.api.admin.service.AdminService;
import com.flipflick.backend.common.response.ApiResponse;
import com.flipflick.backend.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
@Tag(name="Admin", description = "Admin 관련 API 입니다.")
public class AdminController {

    private final AdminService adminService;

    @Operation(summary = "대시보드 통계 조회", description = "회원, 리뷰, 토론 통계 조회")
    @GetMapping("/stat")
    public ResponseEntity<ApiResponse<DashboardStatResponseDto>> getDashboardStat() {
        System.out.println("시작");

        DashboardStatResponseDto stats = adminService.getDashboardStats();
        return ApiResponse.success(SuccessStatus.DASHBOARD_STAT_READ_SUCCESS,stats);


    }

    @Operation(summary = "회원 팝콘 등급 분포 조회", description = "회원 팝콘 등급 분포 조회")
    @GetMapping("/popcorn")
    public ResponseEntity<ApiResponse<List<PopcornGradeResponseDto>>> getPopcornStats() {
        List<PopcornGradeResponseDto> popcornGradeResponseDtos = adminService.getUserCountByPopcornGrade();
        return ApiResponse.success(SuccessStatus.POPCORN_GRADE_STAT_READ_SUCCESS,popcornGradeResponseDtos);
    }

    @Operation(summary = "리뷰 많은 영화 Top 5", description = "리뷰 수 기준 상위 5개 영화 조회")
    @GetMapping("/movies/top-review")
    public ResponseEntity<ApiResponse<List<MovieReviewCountResponseDto>>>getTopReviewedMovies() {
        List<MovieReviewCountResponseDto> topMovies = adminService.getTop5MoviesByReviewCount();
        return ApiResponse.success(SuccessStatus.TOP_MOVIES_BY_REVIEW_SUCCESS, topMovies);
    }

    @Operation(summary = "회원 상태 변경 API", description = "회원 상태 변경")
    @PatchMapping("/member/{memberId}/status")
    public ResponseEntity<ApiResponse<Void>> updateMemberStatus(
            @PathVariable Long memberId,
            @RequestBody MemberStatusUpdateRequestDto request
    ) {
        adminService.updateMemberStatus(memberId, request.getStatus());
        return ApiResponse.success_only(SuccessStatus.MEMBER_STATUS_UPDATE_SUCCESS);
    }
}
