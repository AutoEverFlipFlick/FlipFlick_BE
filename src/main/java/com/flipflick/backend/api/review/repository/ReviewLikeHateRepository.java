package com.flipflick.backend.api.review.repository;

import com.flipflick.backend.api.review.entity.ReviewLikeHate;
import com.flipflick.backend.api.review.entity.LikeHateType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ReviewLikeHateRepository extends JpaRepository<ReviewLikeHate, Long> {

    // 특정 사용자의 특정 리뷰에 대한 좋아요/싫어요 조회
    @Query("SELECT rlh FROM ReviewLikeHate rlh " +
            "WHERE rlh.review.id = :reviewId AND rlh.member.id = :memberId")
    Optional<ReviewLikeHate> findByReviewIdAndMemberId(@Param("reviewId") Long reviewId, @Param("memberId") Long memberId);

    // 특정 리뷰의 좋아요 수
    @Query("SELECT COUNT(rlh) FROM ReviewLikeHate rlh " +
            "WHERE rlh.review.id = :reviewId AND rlh.type = 'LIKE'")
    Long countLikesByReviewId(@Param("reviewId") Long reviewId);

    // 특정 리뷰의 싫어요 수
    @Query("SELECT COUNT(rlh) FROM ReviewLikeHate rlh " +
            "WHERE rlh.review.id = :reviewId AND rlh.type = 'HATE'")
    Long countHatesByReviewId(@Param("reviewId") Long reviewId);


    // 수정: 특정 날짜 끝 시점의 좋아요 수 계산
    @Query("SELECT COUNT(rlh) FROM ReviewLikeHate rlh " +
            "WHERE rlh.review.member.id = :memberId AND rlh.type = 'LIKE' " +
            "AND rlh.createdAt < :endOfDay")
    Integer countLikesByMemberIdUntilDate(@Param("memberId") Long memberId,
                                          @Param("endOfDay") LocalDateTime endOfDay);

    // 수정: 특정 날짜 끝 시점의 싫어요 수 계산
    @Query("SELECT COUNT(rlh) FROM ReviewLikeHate rlh " +
            "WHERE rlh.review.member.id = :memberId AND rlh.type = 'HATE' " +
            "AND rlh.createdAt < :endOfDay")
    Integer countHatesByMemberIdUntilDate(@Param("memberId") Long memberId,
                                          @Param("endOfDay") LocalDateTime endOfDay);
}