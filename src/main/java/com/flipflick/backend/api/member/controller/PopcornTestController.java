package com.flipflick.backend.api.member.controller;

import com.flipflick.backend.api.follow.repository.FollowRepository;
import com.flipflick.backend.api.member.entity.DailyExpLog;
import com.flipflick.backend.api.member.entity.Member;
import com.flipflick.backend.api.member.repository.DailyExpLogRepository;
import com.flipflick.backend.api.member.repository.MemberRepository;
import com.flipflick.backend.api.member.service.PopcornScoreService;
import com.flipflick.backend.api.review.repository.ReviewLikeHateRepository;
import com.flipflick.backend.api.review.repository.ReviewRepository;
import com.flipflick.backend.common.response.ApiResponse;
import com.flipflick.backend.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RestController
@Slf4j
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
public class PopcornTestController {
    private final MemberRepository memberRepository;
    private final DailyExpLogRepository dailyExpLogRepository;
    private final FollowRepository followRepository;
    private final ReviewLikeHateRepository reviewLikeHateRepository;
    private final ReviewRepository reviewRepository;
    private final PopcornScoreService popcornScoreService;

    @Operation(summary = "팝콘지수 초기화 (테스트용)", description = "모든 사용자의 팝콘지수를 초기화합니다.")
    @PostMapping("/reset-all")
    public ResponseEntity<ApiResponse<String>> resetAllPopcornScores() {
        List<Member> allMembers = memberRepository.findAll();

        for (Member member : allMembers) {
            member.resetPopcornScore();
            memberRepository.save(member);
        }

        return ApiResponse.success(SuccessStatus.SEND_POPCORN_SCORE_SUCCESS, "모든 팝콘지수 초기화 완료");
    }

    @Operation(summary = "경험치 로그 초기화 (테스트용)", description = "모든 경험치 로그를 삭제합니다.")
    @DeleteMapping("/reset-logs")
    public ResponseEntity<ApiResponse<String>> resetAllLogs() {
        dailyExpLogRepository.deleteAll();
        return ApiResponse.success(SuccessStatus.SEND_POPCORN_SCORE_SUCCESS, "모든 경험치 로그 삭제 완료");
    }

    // ...existing code...

    @Operation(summary = "특정 날짜 강제 계산 (테스트용)", description = "특정 날짜의 팝콘지수를 강제로 계산합니다.")
    @PostMapping("/calculate-date")
    public ResponseEntity<ApiResponse<String>> calculateByDate(
            @Parameter(description = "계산할 날짜", example = "2025-07-17")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {

        log.info("🧪 테스트 계산 시작 - 날짜: {}", date);

        // 🎯 수정: 해당 날짜의 모든 로그를 미처리 상태로 변경 (강제 재계산)
        List<DailyExpLog> logs = dailyExpLogRepository.findByLogDate(date);
        for (DailyExpLog log : logs) {
            log.setProcessed(false);
            dailyExpLogRepository.save(log);
        }

        List<Member> allMembers = memberRepository.findAll();
        int processedCount = 0;
        int updatedCount = 0;

        for (Member member : allMembers) {
            try {
                log.debug("🔄 사용자 {} 처리 중...", member.getNickname());

                DailyExpLog dailyLog = popcornScoreService.calculateMemberDailyExp(member, date);

                if (dailyLog != null) {
                    // 누적 경험치 업데이트
                    member.updateTotalExp(dailyLog.getDailyExp());
                    memberRepository.save(member);

                    // 처리 완료 표시
                    dailyLog.markAsProcessed();
                    dailyExpLogRepository.save(dailyLog);

                    updatedCount++;

                    log.debug("✅ 사용자 {} 업데이트 완료: 팝콘지수 {}, 경험치 증가 {}",
                            member.getNickname(), member.getPopcorn(), dailyLog.getDailyExp());
                } else {
                    log.debug("⏭️ 사용자 {} 건너뜀 (이미 처리됨 또는 활동 없음)", member.getNickname());
                }

                processedCount++;

            } catch (Exception e) {
                log.error("❌ 사용자 {} 처리 실패: {}", member.getNickname(), e.getMessage());
            }
        }

        log.info("🧪 테스트 계산 완료 - 처리: {}, 업데이트: {}", processedCount, updatedCount);

        return ApiResponse.success(SuccessStatus.SEND_POPCORN_SCORE_SUCCESS,
                String.format("처리: %d명, 업데이트: %d명", processedCount, updatedCount));
    }
}
