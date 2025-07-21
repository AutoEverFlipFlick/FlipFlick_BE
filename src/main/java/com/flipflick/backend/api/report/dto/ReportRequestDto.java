package com.flipflick.backend.api.report.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReportRequestDto {

    private Long reporterId;         // 신고자 ID
    private Long targetId;           // 피신고자 ID
    private String type;             // 신고 대상 타입: "리뷰", "토론", "댓글"
    private String content;          // 신고 사유
    private String targetTitle;      // 신고된 글 제목
    private String targetContent;    // 신고된 글 내용
    private Long targetEntityId;     // 신고된 엔티티 ID (리뷰/토론/댓글 ID)


}
