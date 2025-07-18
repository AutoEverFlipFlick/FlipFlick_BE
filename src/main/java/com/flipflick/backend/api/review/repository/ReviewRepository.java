package com.flipflick.backend.api.review.repository;

import com.flipflick.backend.api.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 특정 영화의 리뷰 조회 (삭제되지 않은 것만, 최신순)
    @Query("SELECT r FROM Review r " +
            "WHERE r.movie.tmdbId = :tmdbId AND r.isDeleted = false " +
            "ORDER BY r.createdAt DESC")
    Page<Review> findByMovieTmdbIdAndIsDeletedFalseOrderByCreatedAtDesc(@Param("tmdbId") Long tmdbId, Pageable pageable);

    // 특정 영화의 리뷰 조회 (삭제되지 않은 것만, 인기순)
    @Query("SELECT r FROM Review r " +
            "WHERE r.movie.tmdbId = :tmdbId AND r.isDeleted = false " +
            "ORDER BY r.likeCnt DESC, r.createdAt DESC")
    Page<Review> findByMovieTmdbIdAndIsDeletedFalseOrderByLikeCntDesc(@Param("tmdbId") Long tmdbId, Pageable pageable);

    // 특정 사용자와 영화의 리뷰 조회 (중복 리뷰 방지용)
    @Query("SELECT r FROM Review r " +
            "WHERE r.member.id = :memberId AND r.movie.tmdbId = :tmdbId AND r.isDeleted = false")
    Optional<Review> findByMemberIdAndMovieTmdbIdAndIsDeletedFalse(@Param("memberId") Long memberId, @Param("tmdbId") Long tmdbId);

    // 특정 리뷰 조회 (삭제되지 않은 것만)
    @Query("SELECT r FROM Review r WHERE r.id = :reviewId AND r.isDeleted = false")
    Optional<Review> findByIdAndIsDeletedFalse(@Param("reviewId") Long reviewId);

    // 특정 영화의 평점 계산용
    @Query("SELECT AVG(r.star) FROM Review r WHERE r.movie.tmdbId = :tmdbId AND r.isDeleted = false")
    Double calculateAverageStarByMovieTmdbId(@Param("tmdbId") Long tmdbId);

    // 특정 영화의 리뷰 개수
    @Query("SELECT COUNT(r) FROM Review r WHERE r.movie.tmdbId = :tmdbId AND r.isDeleted = false")
    Long countByMovieTmdbIdAndIsDeletedFalse(@Param("tmdbId") Long tmdbId);

    // 닉네임으로 리뷰 조회 (최신순)
    @Query("SELECT r FROM Review r " +
            "WHERE r.member.nickname = :nickname AND r.isDeleted = false " +
            "ORDER BY r.createdAt DESC")
    Page<Review> findByMemberNicknameAndIsDeletedFalseOrderByCreatedAtDesc(@Param("nickname") String nickname, Pageable pageable);

}