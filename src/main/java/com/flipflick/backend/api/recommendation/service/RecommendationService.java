package com.flipflick.backend.api.recommendation.service;

import com.flipflick.backend.api.member.repository.MemberRepository;
import com.flipflick.backend.api.recommendation.dto.RecommendationDataDto;
import com.flipflick.backend.api.recommendation.dto.SimilarityBatchDto;
import com.flipflick.backend.api.recommendation.entity.UserSimilarity;
import com.flipflick.backend.api.recommendation.repository.UserSimilarityRepository;
import com.flipflick.backend.api.review.dto.ReviewResponseDto;
import com.flipflick.backend.api.review.entity.Review;
import com.flipflick.backend.api.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RecommendationService {

    private final ReviewRepository reviewRepository;
    private final MemberRepository memberRepository;
    private final UserSimilarityRepository userSimilarityRepository;
    private final RestTemplate restTemplate;

    @Value("${python.recommendation.server.url}")
    private String pythonServerUrl;

    // 🎯 추가: 유사한 성향 사용자들의 리뷰 조회
    public ReviewResponseDto.PageResponse getSimilarUserReviews(Long memberId, int page, int size) {
        // 1. 캐시된 유사 사용자 목록 조회
        List<Long> similarUserIds = getSimilarUserIds(memberId);
        
        if (similarUserIds.isEmpty()) {
            // 유사 사용자가 없으면 빈 결과 반환
            return ReviewResponseDto.PageResponse.builder()
                    .reviews(Collections.emptyList())
                    .currentPage(page)
                    .totalPages(0)
                    .totalElements(0L)
                    .first(true)
                    .last(true)
                    .empty(true)
                    .build();
        }
        
        // 2. 유사 사용자들의 리뷰 조회 (본인 제외, 평점 4점 이상만)
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Review> reviewPage = reviewRepository.findHighRatedReviewsBySimilarUsers(
                similarUserIds, memberId, 4.0, pageable);
        
        // 3. DTO 변환
        Page<ReviewResponseDto.Detail> detailPage = reviewPage.map(this::convertToDetail);
        return ReviewResponseDto.PageResponse.from(detailPage);
    }

    // Python 서버용 데이터 추출 (Native Query 결과 처리)
    public List<RecommendationDataDto> exportRecommendationData() {
        List<Object[]> results = reviewRepository.findRecommendationDataNative();

        return results.stream()
                .map(row -> {
                    // 장르 문자열을 배열로 변환
                    String genresString = (String) row[3];
                    List<String> genres = genresString != null ?
                            Arrays.asList(genresString.split(",")) :
                            Collections.emptyList();

                    return RecommendationDataDto.builder()
                            .memberId(((Number) row[0]).longValue())
                            .movieId(((Number) row[1]).longValue())
                            .rating(((Number) row[2]).doubleValue())
                            .genres(genres) // 장르 배열
                            .tmdbId(row[4] != null ? ((Number) row[4]).longValue() : null)
                            .movieTitle((String) row[5])
                            .build();
                })
                .collect(Collectors.toList());
    }

    //캐시된 유사 사용자 목록 조회
    private List<Long> getSimilarUserIds(Long memberId) {
        List<UserSimilarity> similarities = userSimilarityRepository
                .findTop10ByMemberIdOrderBySimilarityScoreDesc(memberId);
        
        return similarities.stream()
                .map(UserSimilarity::getSimilarMemberId)
                .collect(Collectors.toList());
    }

    // Python 서버에 유사도 재계산 요청
    @Async
    public void triggerSimilarityRecalculation() {
        try {
            String url = pythonServerUrl + "/recalculate-similarity";
            restTemplate.postForObject(url, null, String.class);
            log.info("Python 서버에 유사도 재계산 요청 완료");
        } catch (Exception e) {
            log.error("Python 서버 요청 실패: {}", e.getMessage());
        }
    }

    // 유사도 데이터 저장
    @Transactional
    public void saveSimilarities(SimilarityBatchDto batch) {
        List<UserSimilarity> similarities = batch.getSimilarities().stream()
                .map(sim -> UserSimilarity.builder()
                        .memberId(batch.getMemberId())
                        .similarMemberId(sim.getSimilarUserId())
                        .similarityScore(sim.getSimilarityScore())
                        .calculatedAt(LocalDateTime.now())
                        .build())
                .collect(Collectors.toList());
        
        userSimilarityRepository.saveAll(similarities);
    }

    //기존 유사도 데이터 삭제
    @Transactional
    public void deleteAllSimilarities() {
        userSimilarityRepository.deleteAll();
    }

    // Review 엔티티를 Detail DTO로 변환
    private ReviewResponseDto.Detail convertToDetail(Review review) {
        return ReviewResponseDto.Detail.builder()
                .reviewId(review.getId())
                .memberId(review.getMember().getId())
                .content(review.getContent())
                .star(review.getStar())
                .spoiler(review.getSpoiler())
                .likeCnt(review.getLikeCnt())
                .hateCnt(review.getHateCnt())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .nickname(review.getMember().getNickname())
                .profileImage(review.getMember().getProfileImage())
                .popcorn(review.getMember().getPopcorn())
                .build();
    }

    // 오래된 유사도 데이터 정리 (스케줄러)
    @Scheduled(cron = "0 0 4 * * *") // 매일 새벽 4시
    @Transactional
    public void cleanupOldSimilarities() {
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
        userSimilarityRepository.deleteByCalculatedAtBefore(threeDaysAgo);
        log.info("오래된 유사도 데이터 정리 완료");
    }
}