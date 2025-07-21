package com.flipflick.backend.api.recommendation.repository;

import com.flipflick.backend.api.recommendation.entity.UserSimilarity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserSimilarityRepository extends JpaRepository<UserSimilarity, Long> {

    // 특정 사용자와 유사한 상위 N명 조회
    List<UserSimilarity> findTop10ByMemberIdOrderBySimilarityScoreDesc(Long memberId);

    // 특정 사용자의 유사도 데이터 삭제 (재계산 전)
    void deleteByMemberId(Long memberId);

    // 오래된 유사도 데이터 삭제
    void deleteByCalculatedAtBefore(LocalDateTime dateTime);
}