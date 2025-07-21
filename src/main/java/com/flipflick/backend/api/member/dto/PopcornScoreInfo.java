package com.flipflick.backend.api.member.dto;

import com.flipflick.backend.api.member.entity.DailyExpLog;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "팝콘지수 정보")
public class PopcornScoreInfo {

    @Schema(description = "팝콘지수", example = "58.3")
    private Double popcornScore;

    @Schema(description = "총 누적 경험치", example = "1800.0")
    private Double totalExp;

    @Schema(description = "등급", example = "2/3 팝콘")
    private String grade;

    @Schema(description = "차단 횟수", example = "0")
    private Integer blockCount;

    @Schema(description = "최근 7일 경험치 로그")
    private List<DailyExpLog> recentLogs;
}