package com.flipflick.backend.api.member.service;

import com.flipflick.backend.api.follow.repository.FollowRepository;
import com.flipflick.backend.api.member.dto.PopcornScoreInfo;
import com.flipflick.backend.api.member.entity.DailyExpLog;
import com.flipflick.backend.api.member.entity.Member;
import com.flipflick.backend.api.member.repository.DailyExpLogRepository;
import com.flipflick.backend.api.member.repository.MemberRepository;
import com.flipflick.backend.api.review.repository.ReviewLikeHateRepository;
import com.flipflick.backend.api.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PopcornScoreService {

    private final MemberRepository memberRepository;
    private final DailyExpLogRepository dailyExpLogRepository;
    private final FollowRepository followRepository;
    private final ReviewLikeHateRepository reviewLikeHateRepository;
    private final ReviewRepository reviewRepository;

    //매일 자정에 실행
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void calculateDailyExperience() {
        log.info("일일 팝콘지수 계산 시작");

        // 수정: 미처리된 지난 7일 데이터도 함께 처리
        LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);
        LocalDate yesterday = LocalDate.now().minusDays(1);

        List<Member> allMembers = memberRepository.findAll();
        int processedCount = 0;
        int updatedCount = 0;

        for (Member member : allMembers) {
            try {
                // 수정: 지난 7일간 미처리된 날짜들을 모두 처리
                for (LocalDate date = sevenDaysAgo; !date.isAfter(yesterday); date = date.plusDays(1)) {
                    DailyExpLog dailyLog = calculateMemberDailyExp(member, date);

                    if (dailyLog != null) {
                        member.updateTotalExp(dailyLog.getDailyExp());
                        memberRepository.save(member);

                        dailyLog.markAsProcessed();
                        dailyExpLogRepository.save(dailyLog);

                        updatedCount++;

                        log.debug("사용자 {} 날짜 {} 팝콘지수 업데이트: {} (경험치: +{})",
                                member.getNickname(), date, member.getPopcorn(), dailyLog.getDailyExp());
                    }
                }

                processedCount++;

            } catch (Exception e) {
                log.error("사용자 {} 팝콘지수 계산 실패: {}", member.getNickname(), e.getMessage());
            }
        }

        log.info(" 일일 팝콘지수 계산 완료 - 처리: {}, 업데이트: {}", processedCount, updatedCount);
    }

    @Transactional
    public DailyExpLog calculateMemberDailyExp(Member member, LocalDate date) {
        // 기존 로그 확인
        Optional<DailyExpLog> existingLog = dailyExpLogRepository.findByMemberAndLogDate(member, date);

        if (existingLog.isPresent() && existingLog.get().getIsProcessed()) {
            return null;
        }

        DailyExpLog dailyLog = existingLog.orElse(DailyExpLog.builder()
                .member(member)
                .logDate(date)
                .isProcessed(false)
                .build());

        // 🎯 수정: 증감량 계산 방식
        LocalDateTime endOfToday = date.plusDays(1).atStartOfDay();
        LocalDateTime endOfYesterday = date.atStartOfDay();

        // 오늘 끝 시점의 총 수량
        Integer totalFollowsToday = followRepository.countFollowsByMemberIdUntilDate(member.getId(), endOfToday);
        Integer totalLikesToday = reviewLikeHateRepository.countLikesByMemberIdUntilDate(member.getId(), endOfToday);
        Integer totalHatesToday = reviewLikeHateRepository.countHatesByMemberIdUntilDate(member.getId(), endOfToday);

        // 어제 끝 시점의 총 수량
        Integer totalFollowsYesterday = followRepository.countFollowsByMemberIdUntilDate(member.getId(), endOfYesterday);
        Integer totalLikesYesterday = reviewLikeHateRepository.countLikesByMemberIdUntilDate(member.getId(), endOfYesterday);
        Integer totalHatesYesterday = reviewLikeHateRepository.countHatesByMemberIdUntilDate(member.getId(), endOfYesterday);

        // null 처리
        totalFollowsToday = (totalFollowsToday != null) ? totalFollowsToday : 0;
        totalLikesToday = (totalLikesToday != null) ? totalLikesToday : 0;
        totalHatesToday = (totalHatesToday != null) ? totalHatesToday : 0;
        totalFollowsYesterday = (totalFollowsYesterday != null) ? totalFollowsYesterday : 0;
        totalLikesYesterday = (totalLikesYesterday != null) ? totalLikesYesterday : 0;
        totalHatesYesterday = (totalHatesYesterday != null) ? totalHatesYesterday : 0;

        // 🎯 핵심: 증감량 계산 (취소도 반영됨)
        Integer followDelta = totalFollowsToday - totalFollowsYesterday;
        Integer likeDelta = totalLikesToday - totalLikesYesterday;
        Integer hateDelta = totalHatesToday - totalHatesYesterday;

        // 수치 업데이트 및 경험치 계산
        dailyLog.updateCounts(followDelta, likeDelta, hateDelta);

        log.debug("📊 사용자 {} 날짜 {} 증감량: 팔로우 {}, 좋아요 {}, 싫어요 {}, 경험치 {}",
                member.getNickname(), date, followDelta, likeDelta, hateDelta, dailyLog.getDailyExp());

        // 경험치가 0이 아닌 경우에만 저장
        if (dailyLog.getDailyExp() != 0) {
            return dailyExpLogRepository.save(dailyLog);
        }

        // 경험치가 0인 경우에도 처리 완료 표시
        dailyLog.markAsProcessed();
        dailyExpLogRepository.save(dailyLog);

        return null;
    }

    // 팝콘지수 등급 계산
    public String getPopcornGrade(Double popcornScore) {
        if (popcornScore >= 81) return "팝콘기계";
        else if (popcornScore >= 71) return "1 팝콘";
        else if (popcornScore >= 61) return "2/3 팝콘";
        else if (popcornScore >= 51) return "1/3 팝콘";
        else if (popcornScore >= 41) return "빈 팝콘";
        else if (popcornScore >= 31) return "옥수수 3";
        else if (popcornScore >= 21) return "옥수수 2";
        else return "옥수수 1";
    }

    // 사용자 팝콘지수 정보 조회
    public PopcornScoreInfo getPopcornScoreInfo(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        String grade = getPopcornGrade(member.getPopcorn());

        // 최근 7일간 로그 조회
        LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);
        List<DailyExpLog> recentLogs = dailyExpLogRepository.findRecentLogsByMemberId(memberId, sevenDaysAgo);

        return PopcornScoreInfo.builder()
                .popcornScore(member.getPopcorn())
                .totalExp(member.getTotalExp())
                .grade(grade)
                .blockCount(member.getBlockCount())
                .recentLogs(recentLogs)
                .build();
    }

    // 수동 팝콘지수 재계산 (관리자용)
    @Transactional
    public void recalculatePopcornScore(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 모든 일일 로그의 경험치 합산
        List<DailyExpLog> allLogs = dailyExpLogRepository.findRecentLogsByMemberId(memberId, LocalDate.now().minusDays(365));

        double totalExp = allLogs.stream()
                .mapToDouble(DailyExpLog::getDailyExp)
                .sum();

        member.updateTotalExp(totalExp - member.getTotalExp()); // 차이만큼 업데이트
        memberRepository.save(member);

        log.info("사용자 {} 팝콘지수 재계산 완료: {}", member.getNickname(), member.getPopcorn());
    }
}