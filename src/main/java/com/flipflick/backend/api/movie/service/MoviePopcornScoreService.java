package com.flipflick.backend.api.movie.service;

import com.flipflick.backend.api.member.repository.MemberRepository;
import com.flipflick.backend.api.movie.entity.Movie;
import com.flipflick.backend.api.movie.repository.MovieRepository;
import com.flipflick.backend.api.movie.repository.WatchedRepository;
import com.flipflick.backend.api.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MoviePopcornScoreService {

    private final MovieRepository movieRepository;
    private final ReviewRepository reviewRepository;
    private final MemberRepository memberRepository;
    private final WatchedRepository watchedRepository;

    // 최소 신뢰 리뷰 수 (m 값)
    private static final int MIN_CONFIDENCE_REVIEWS = 100;

    /**
     * 모든 영화의 Popcorn 점수를 재계산하고 업데이트
     */
    @Transactional
    public void recalculateAllPopcornScores() {
        log.info("Popcorn 점수 재계산 시작");

        // 전체 회원 수 조회
        long totalUserCount = memberRepository.count();
        log.info("전체 회원 수: {}", totalUserCount);

        // 전체 영화 평균 평점 조회
        Double globalAvgRating = reviewRepository.findGlobalAverageRating();
        if (globalAvgRating == null) {
            globalAvgRating = 3.0; // 기본값
        }
        log.info("전체 평균 평점: {}", globalAvgRating);

        // 모든 영화 조회
        List<Movie> movies = movieRepository.findAll();
        log.info("총 영화 수: {}", movies.size());

        int updatedCount = 0;

        for (Movie movie : movies) {
            try {
                double newScore = calculatePopcornScore(movie, totalUserCount, globalAvgRating);
                movie.updatePopcornScore(newScore);
                updatedCount++;

                if (updatedCount % 100 == 0) {
                    log.info("진행률: {}/{} ({}%)", updatedCount, movies.size(),
                            (updatedCount * 100 / movies.size()));
                }
            } catch (Exception e) {
                log.error("영화 ID {} Popcorn 점수 계산 실패: {}", movie.getId(), e.getMessage());
            }
        }

        log.info("Popcorn 점수 재계산 완료. 업데이트된 영화 수: {}", updatedCount);
    }

    /**
     * 특정 영화의 Popcorn 점수 계산
     */
    @Transactional
    public void recalculateMoviePopcornScore(Long movieId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("영화를 찾을 수 없습니다: " + movieId));

        long totalUserCount = memberRepository.count();
        Double globalAvgRating = reviewRepository.findGlobalAverageRating();
        if (globalAvgRating == null) {
            globalAvgRating = 3.0;
        }

        double newScore = calculatePopcornScore(movie, totalUserCount, globalAvgRating);
        movie.updatePopcornScore(newScore);

        log.info("영화 ID {} Popcorn 점수 업데이트: {}", movieId, newScore);
    }

    /**
     * Bayesian 평균 계산
     */
    private double calculateBayesianAverage(long reviewCount, double movieAvgRating, double globalAvgRating) {
        if (reviewCount == 0) {
            return globalAvgRating;
        }

        return ((reviewCount * movieAvgRating) + (MIN_CONFIDENCE_REVIEWS * globalAvgRating))
                / (reviewCount + MIN_CONFIDENCE_REVIEWS);
    }

    /**
     * 좋아요 비율 계산
     */
    private double calculateLikeRatio(long likes, long hates) {
        long total = likes + hates;
        if (total == 0) {
            return 0.5; // 중립값
        }
        return (double) likes / total;
    }

    /**
     * 봤어요 비율 계산
     */
    private double calculateSeenRatio(long seenCount, long totalUserCount) {
        if (totalUserCount == 0) {
            return 0.0;
        }
        return Math.min(1.0, (double) seenCount / totalUserCount); // 최대 1.0으로 제한
    }

    /**
     * 최종 점수 계산
     */
    private double calculateFinalScore(double bayesianAvg, double likeRatio, double seenRatio) {
        // 각 요소를 0-1 범위로 정규화하고 가중치 적용
        double normalizedRating = bayesianAvg / 5.0; // 5점 만점을 1로 정규화

        return (normalizedRating * 0.5 + likeRatio * 0.3 + seenRatio * 0.2) * 100;
    }

    /**
     * 리뷰 통계 조회 (개별 쿼리)
     */
    private ReviewStats getReviewStats(Long movieId) {
        try {
            Long count = reviewRepository.countReviewsByMovieId(movieId);
            Double average = reviewRepository.findAverageRatingByMovieId(movieId);

            if (count == null) count = 0L;
            if (average == null) average = 0.0;

            log.debug("영화 ID {}: 리뷰 수={}, 평균 평점={}", movieId, count, average);
            return new ReviewStats(count, average);

        } catch (Exception e) {
            log.error("리뷰 통계 조회 실패 - 영화 ID: {}", movieId, e);
            return new ReviewStats(0L, 0.0);
        }
    }

    /**
     * 좋아요/싫어요 통계 조회 (개별 쿼리)
     */
    private LikeHateStats getLikeHateStats(Long movieId) {
        try {
            Integer likes = movieRepository.findLikeCountByMovieId(movieId);
            Integer hates = movieRepository.findHateCountByMovieId(movieId);

            if (likes == null) likes = 0;
            if (hates == null) hates = 0;

            log.debug("영화 ID {}: 좋아요={}, 싫어요={}", movieId, likes, hates);
            return new LikeHateStats(likes.longValue(), hates.longValue());

        } catch (Exception e) {
            log.error("좋아요/싫어요 통계 조회 실패 - 영화 ID: {}", movieId, e);
            return new LikeHateStats(0L, 0L);
        }
    }

    /**
     * 봤어요 수 조회
     */
    private long getSeenCount(Long movieId) {
        try {
            Long seenCount = watchedRepository.countWatchedByMovieId(movieId);
            if (seenCount == null) seenCount = 0L;

            log.debug("영화 ID {}: 봤어요 수={}", movieId, seenCount);
            return seenCount;

        } catch (Exception e) {
            log.error("봤어요 수 조회 실패 - 영화 ID: {}", movieId, e);
            return 0L;
        }
    }

    /**
     * Popcorn 점수 계산 로직 (에러 처리 강화)
     */
    private double calculatePopcornScore(Movie movie, long totalUserCount, double globalAvgRating) {
        try {
            log.debug("영화 ID {} Popcorn 점수 계산 시작", movie.getId());

            // 1. 리뷰 데이터 조회
            ReviewStats reviewStats = getReviewStats(movie.getId());

            // 2. 좋아요/싫어요 데이터 조회
            LikeHateStats likeHateStats = getLikeHateStats(movie.getId());

            // 3. 봤어요 데이터 조회
            long seenCount = getSeenCount(movie.getId());

            // 4. Bayesian 평균 계산
            double bayesianAvg = calculateBayesianAverage(
                    reviewStats.getCount(),
                    reviewStats.getAverage(),
                    globalAvgRating
            );

            // 5. 좋아요 비율 계산
            double likeRatio = calculateLikeRatio(
                    likeHateStats.getLikes(),
                    likeHateStats.getHates()
            );

            // 6. 봤어요 비율 계산
            double seenRatio = calculateSeenRatio(seenCount, totalUserCount);

            // 7. 최종 Popcorn 점수 계산
            double score = calculateFinalScore(bayesianAvg, likeRatio, seenRatio);

            log.info("영화 ID {}: 리뷰수={}, 평점={}, 좋아요비율={}, 봤어요비율={}, 최종점수={}",
                    movie.getId(), reviewStats.getCount(), reviewStats.getAverage(),
                    likeRatio, seenRatio, score);

            return Math.round(score * 10.0) / 10.0; // 소수점 첫째 자리까지

        } catch (Exception e) {
            log.error("영화 ID {} Popcorn 점수 계산 중 예외 발생", movie.getId(), e);
            return 0.0; // 기본값 반환
        }
    }

    // 내부 클래스들
    private static class ReviewStats {
        private final long count;
        private final double average;

        public ReviewStats(long count, double average) {
            this.count = count;
            this.average = average;
        }

        public long getCount() { return count; }
        public double getAverage() { return average; }
    }

    private static class LikeHateStats {
        private final long likes;
        private final long hates;

        public LikeHateStats(long likes, long hates) {
            this.likes = likes;
            this.hates = hates;
        }

        public long getLikes() { return likes; }
        public long getHates() { return hates; }
    }
}