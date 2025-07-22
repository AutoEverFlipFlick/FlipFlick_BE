package com.flipflick.backend.api.movie.repository;

import com.flipflick.backend.api.movie.entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    @EntityGraph(attributePaths = {
            "movieGenres.genre",
            "media",
            "providers.provider"
    })
    Optional<Movie> findWithAllByTmdbId(Long tmdbId);
    Optional<Movie> findByTmdbId(Long tmdbId);

    /**
     * 특정 영화의 좋아요 수 조회
     */
    @Query("SELECT m.likeCnt FROM Movie m WHERE m.id = :movieId")
    Integer findLikeCountByMovieId(@Param("movieId") Long movieId);

    /**
     * 특정 영화의 싫어요 수 조회
     */
    @Query("SELECT m.hateCnt FROM Movie m WHERE m.id = :movieId")
    Integer findHateCountByMovieId(@Param("movieId") Long movieId);

    /**
     * Popcorn 점수 기준 TOP 영화 조회 (리뷰 수 3개 이상)
     */
    @Query("SELECT m FROM Movie m WHERE (SELECT COUNT(r) FROM Review r WHERE r.movie.id = m.id AND r.isDeleted = false) >= 3 ORDER BY m.popcorn DESC")
    Page<Movie> findTopMoviesByPopcornScore(Pageable pageable);
}
