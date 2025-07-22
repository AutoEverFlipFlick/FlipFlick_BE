package com.flipflick.backend.api.movie.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.flipflick.backend.api.movie.service.MovieService;

import java.time.LocalDate;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
@Slf4j
public class BoxOfficeCacheScheduler {

    private final MovieService movieService;

    /**
     * 매일 새벽 00:01 (Asia/Seoul) 에 어제 날짜 박스오피스 API를 미리 캐싱
     * cron: second(0) minute(1) hour(0) day(*) month(*) weekday(*)
     */
    @Scheduled(cron = "0 1 0 * * *", zone = "Asia/Seoul")
    public void preCacheDailyBoxOffice() {
        // 오늘 날짜 (YYYY-MM-DD)
        String today = LocalDate.now(ZoneId.of("Asia/Seoul")).toString();
        try {
            movieService.getYesterdayBoxOffice(today);
            log.info("[Scheduler] BoxOffice 캐시 성공: {}", today);
        } catch (Exception e) {
            log.error("[Scheduler] BoxOffice 캐시 실패", e);
        }
    }
}