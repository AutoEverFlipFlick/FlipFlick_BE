package com.flipflick.backend.api.report.controller;

import com.flipflick.backend.api.debate.dto.DebateCommentRequestDto;
import com.flipflick.backend.api.report.dto.ReportRequestDto;
import com.flipflick.backend.api.report.service.ReportService;
import com.flipflick.backend.common.config.security.SecurityMember;
import com.flipflick.backend.common.response.ApiResponse;
import com.flipflick.backend.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/report")
@RequiredArgsConstructor
@Tag(name = "Report", description = "신고 API")
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "신고 등록 API", description = "신고 등록")
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createReport(@RequestBody ReportRequestDto requestDto) {
        reportService.createReport(requestDto);
        return ApiResponse.success_only(SuccessStatus.REPORT_CREATE_SUCCESS);
    }

    @Operation(summary = "신고 삭제 API", description = "신고 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReport(@PathVariable Long id) {
        reportService.deleteReport(id);
        return ApiResponse.success_only(SuccessStatus.REPORT_CREATE_SUCCESS);
    }


}