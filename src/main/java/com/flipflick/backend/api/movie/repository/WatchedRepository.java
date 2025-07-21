package com.flipflick.backend.api.movie.repository;

import com.flipflick.backend.api.member.entity.Member;
import com.flipflick.backend.api.movie.entity.Movie;
import com.flipflick.backend.api.movie.entity.Watched;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface WatchedRepository extends JpaRepository<Watched, Long> {

    Optional<Watched> findByMemberAndMovie(Member member, Movie movie);
    void deleteByMemberAndMovie(Member member, Movie movie);
    boolean existsByMemberAndMovie(Member member, Movie movie);

    // Movie 연관관계를 한 번에 패치해서 N+1 방지
    @EntityGraph(attributePaths = {"movie"})
    Page<Watched> findByMember_Id(Long memberId, Pageable pageable);
    /**
     * 특정 영화를 본 사용자 수 조회
     */
    @Query("SELECT COUNT(w) FROM Watched w WHERE w.movie.id = :movieId")
    Long countWatchedByMovieId(@Param("movieId") Long movieId);
}
