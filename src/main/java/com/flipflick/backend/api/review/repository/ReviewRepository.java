package com.flipflick.backend.api.review.repository;

import com.flipflick.backend.api.admin.dto.MovieReviewCountResponseDto;
import com.flipflick.backend.api.member.entity.Member;
import com.flipflick.backend.api.recommendation.dto.RecommendationDataDto;
import com.flipflick.backend.api.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 특정 영화의 리뷰 조회 (삭제되지 않은 것만, 최신순)
    @Query("SELECT r FROM Review r " +
            "WHERE r.movie.tmdbId = :tmdbId AND r.isDeleted = false AND r.member.isDeleted = false " +
            "ORDER BY r.createdAt DESC")
    Page<Review> findByMovieTmdbIdAndIsDeletedFalseOrderByCreatedAtDesc(@Param("tmdbId") Long tmdbId, Pageable pageable);

    // 특정 영화의 리뷰 조회 (삭제되지 않은 것만, 인기순)
    @Query("SELECT r FROM Review r " +
            "WHERE r.movie.tmdbId = :tmdbId AND r.isDeleted = false AND r.member.isDeleted = false " +
            "ORDER BY r.likeCnt DESC, r.createdAt DESC")
    Page<Review> findByMovieTmdbIdAndIsDeletedFalseOrderByLikeCntDesc(@Param("tmdbId") Long tmdbId, Pageable pageable);

    // 특정 사용자와 영화의 리뷰 조회 (중복 리뷰 방지용)
    @Query("SELECT r FROM Review r " +
            "WHERE r.member.id = :memberId AND r.movie.tmdbId = :tmdbId AND r.isDeleted = false")
    Optional<Review> findByMemberIdAndMovieTmdbIdAndIsDeletedFalse(@Param("memberId") Long memberId, @Param("tmdbId") Long tmdbId);

    // 특정 리뷰 조회 (삭제되지 않은 것만)
    @Query("SELECT r FROM Review r WHERE r.id = :reviewId AND r.isDeleted = false AND r.member.isDeleted = false")
    Optional<Review> findByIdAndIsDeletedFalse(@Param("reviewId") Long reviewId);

    // 특정 영화의 평점 계산용
    @Query("SELECT AVG(r.star) FROM Review r WHERE r.movie.tmdbId = :tmdbId AND r.isDeleted = false AND r.member.isDeleted = false")
    Double calculateAverageStarByMovieTmdbId(@Param("tmdbId") Long tmdbId);

    // 특정 영화의 리뷰 개수
    @Query("SELECT COUNT(r) FROM Review r WHERE r.movie.tmdbId = :tmdbId AND r.isDeleted = false")
    Long countByMovieTmdbIdAndIsDeletedFalse(@Param("tmdbId") Long tmdbId);

    // 닉네임으로 리뷰 조회 (최신순)
    @Query("SELECT r FROM Review r " +
            "WHERE r.member.nickname = :nickname AND r.isDeleted = false " +
            "ORDER BY r.createdAt DESC")
    Page<Review> findByMemberNicknameAndIsDeletedFalseOrderByCreatedAtDesc(@Param("nickname") String nickname, Pageable pageable);

    // 날짜별 전체 리뷰 수
    @Query(value = """
        SELECT CAST(r.created_at AS DATE) AS date, COUNT(*) AS count
        FROM review r
        WHERE CAST(r.created_at AS DATE) <= :endDate
        GROUP BY CAST(r.created_at AS DATE)
        ORDER BY CAST(r.created_at AS DATE)
    """, nativeQuery = true)
    List<Object[]> countReviewsUntilDate(@Param("endDate") LocalDate endDate);

    // 날짜별 신규 리뷰 수
    @Query(value = """
        SELECT CAST(r.created_at AS DATE) AS date, COUNT(*) AS count
        FROM review r
        WHERE CAST(r.created_at AS DATE) BETWEEN :startDate AND :endDate
        GROUP BY CAST(r.created_at AS DATE)
        ORDER BY CAST(r.created_at AS DATE)
    """, nativeQuery = true)
    List<Object[]> countNewReviewsByDate(@Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate);


    @Query("SELECT new com.flipflick.backend.api.admin.dto.MovieReviewCountResponseDto(r.movie.title, COUNT(r)) " +
            "FROM Review r GROUP BY r.movie.id, r.movie.title ORDER BY COUNT(r) DESC")
    List<MovieReviewCountResponseDto> findTop5MoviesByReviewCount(Pageable pageable);

    int countByMember(Member member);

    // Python 서버용 추천 데이터 추출
    @Query(value = """
    SELECT 
        r.member_id,
        r.movie_id,
        r.star,
        GROUP_CONCAT(g.genre_name ORDER BY g.genre_name SEPARATOR ',') as genres,
        m.tmdb_id,
        m.title
    FROM review r 
    JOIN movie m ON r.movie_id = m.id
    JOIN movie_genre mg ON mg.movie_id = m.id
    JOIN genre g ON g.id = mg.genre_id
    WHERE r.is_deleted = false
      AND EXISTS (
          SELECT 1 FROM movie_watched w 
          WHERE w.member_id = r.member_id 
            AND w.movie_id = r.movie_id
      )
    GROUP BY r.member_id, r.movie_id, r.star, m.tmdb_id, m.title
    ORDER BY r.member_id, r.movie_id
    """, nativeQuery = true)
    List<Object[]> findRecommendationDataNative();

    // 유사 사용자들의 고평점 리뷰 조회
    @Query("""
    SELECT r FROM Review r 
    WHERE r.member.id IN :similarUserIds 
    AND r.member.id != :excludeMemberId
    AND r.star >= :minRating
    AND r.isDeleted = false
    ORDER BY r.star DESC, r.likeCnt DESC
    """)
    Page<Review> findHighRatedReviewsBySimilarUsers(
            @Param("similarUserIds") List<Long> similarUserIds,
            @Param("excludeMemberId") Long excludeMemberId,
            @Param("minRating") Double minRating,
            Pageable pageable);

    /**
     * 전체 평균 평점 조회
     */
    @Query("SELECT AVG(r.star) FROM Review r WHERE r.isDeleted = false")
    Double findGlobalAverageRating();

    /**
     * 특정 영화의 리뷰 수 조회
     */
    @Query("SELECT COUNT(r) FROM Review r WHERE r.movie.id = :movieId AND r.isDeleted = false")
    Long countReviewsByMovieId(@Param("movieId") Long movieId);

    /**
     * 특정 영화의 평균 평점 조회
     */
    @Query("SELECT AVG(r.star) FROM Review r WHERE r.movie.id = :movieId AND r.isDeleted = false")
    Double findAverageRatingByMovieId(@Param("movieId") Long movieId);
}