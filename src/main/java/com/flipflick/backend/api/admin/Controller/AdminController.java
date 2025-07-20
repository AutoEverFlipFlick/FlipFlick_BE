package com.flipflick.backend.api.admin.Controller;

import com.flipflick.backend.api.admin.dto.*;
import com.flipflick.backend.api.admin.service.AdminService;
import com.flipflick.backend.api.member.dto.MemberListResponseDto;
import com.flipflick.backend.common.response.ApiResponse;
import com.flipflick.backend.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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

    @Operation(summary = "회원 목록 조회 API", description = "회원 조회합니다.")
    @GetMapping("/members")
    public ResponseEntity<ApiResponse<Page<MemberListResponseDto>>> getMembers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword
    ) {


        Page<MemberListResponseDto> memberListResponseDto = adminService.getMembersWithStats(page, size, keyword);

        return ApiResponse.success(SuccessStatus.MEMBER_LIST_SUCCESS,memberListResponseDto);
    }

    @Operation(summary = "신고 처리 API", description = "신고 ID와 액션을 받아 신고 처리")
    @PatchMapping("/report/{reportId}/handle")
    public ResponseEntity<ApiResponse<Void>> handleReport(
            @PathVariable Long reportId,
            @RequestBody ReportActionRequestDto request
    ) {
        adminService.processReport(reportId, request);
        return ApiResponse.success_only(SuccessStatus.REPORT_UPDATE_SUCCESS);
    }

    @Operation(summary = "신고 목록 조회 API", description = "신고 목록을 조회합니다.")
    @GetMapping("/reports")
    public ResponseEntity<ApiResponse<Page<ReportAdminResponseDto>>> getReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword, // 신고자 닉네임 검색
            @RequestParam(required = false, defaultValue = "전체") String status // 전체 / 처리 / 미처리
    ) {
        Page<ReportAdminResponseDto> result = adminService.getReportsWithFilter(page, size, keyword, status);
        return ApiResponse.success(SuccessStatus.REPORT_LIST_SUCCESS, result);
    }

}
