package com.flipflick.backend.api.movie.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MoviePopcornScoreScheduler {

    private final MoviePopcornScoreService moviePopcornScoreService;

    /**
     * 매일 새벽 12시에 Popcorn 점수 재계산
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void recalculatePopcornScores() {
        log.info("스케줄러: Popcorn 점수 재계산 시작");

        try {
            moviePopcornScoreService.recalculateAllPopcornScores();
            log.info("스케줄러: Popcorn 점수 재계산 완료");
        } catch (Exception e) {
            log.error("스케줄러: Popcorn 점수 재계산 실패", e);
        }
    }
}