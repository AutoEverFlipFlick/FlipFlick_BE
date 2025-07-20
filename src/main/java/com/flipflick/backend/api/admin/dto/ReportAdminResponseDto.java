package com.flipflick.backend.api.admin.dto;

import com.flipflick.backend.api.member.entity.Member;
import com.flipflick.backend.api.report.entity.Report;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportAdminResponseDto {

    private Long reportId;

    private String reporterNickname;
    private String targetNickname;

    private String type;           // 리뷰, 댓글, 게시글
    private String content;        // 신고 사유
    private String targetTitle;    // 신고된 글 제목 (nullable)
    private String targetContent;  // 신고된 글 본문

    private String handled;
    private LocalDateTime createdAt;

    private int warningCount;
    private int suspensionCount;
    private String targetStatus;   // 활동중 / 정지 / 차단

    public static ReportAdminResponseDto from(Report report) {
        Member target = report.getTarget();
        return ReportAdminResponseDto.builder()
                .reportId(report.getId())
                .reporterNickname(report.getReporter().getNickname())
                .targetNickname(target.getNickname())
                .type(report.getType())
                .content(report.getContent())
                .targetTitle(report.getTargetTitle())
                .targetContent(report.getTargetContent())
                .handled(getHandledText(report.isHandled()))
                .createdAt(report.getCreatedAt())
                .warningCount(target.getWarnCount() != null ? target.getWarnCount() : 0)
                .suspensionCount(target.getBlockCount() != null ? target.getBlockCount() : 0)
                .targetStatus(getStatusText(target.getBlock()))
                .build();
    }

    private static String getStatusText(Integer block) {
        if (block == null) return "정상";
        return switch (block) {
            case 1 -> "정지";
            case 2 -> "차단";
            default -> "정상";
        };
    }

    private static String getHandledText(boolean handled) {
        return handled ? "처리" : "미처리";
    }
}

