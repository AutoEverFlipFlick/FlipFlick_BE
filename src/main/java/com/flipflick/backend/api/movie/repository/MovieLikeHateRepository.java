package com.flipflick.backend.api.movie.repository;

import com.flipflick.backend.api.member.entity.Member;
import com.flipflick.backend.api.movie.entity.Movie;
import com.flipflick.backend.api.movie.entity.MovieLikeHate;
import com.flipflick.backend.api.review.entity.LikeHateType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MovieLikeHateRepository extends JpaRepository<MovieLikeHate, Long> {

    Optional<MovieLikeHate> findByMemberAndMovie(Member member, Movie movie);
    boolean existsByMemberAndMovieAndType(Member member, Movie movie, LikeHateType type);

    @EntityGraph(attributePaths = {"movie"})
    Page<MovieLikeHate> findByMember_IdAndType(Long memberId, LikeHateType type, Pageable pageable);
}
