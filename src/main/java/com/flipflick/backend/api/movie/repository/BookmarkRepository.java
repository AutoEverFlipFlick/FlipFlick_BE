package com.flipflick.backend.api.movie.repository;

import com.flipflick.backend.api.member.entity.Member;
import com.flipflick.backend.api.movie.entity.Bookmark;
import com.flipflick.backend.api.movie.entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    Optional<Bookmark> findByMemberAndMovie(Member member, Movie movie);
    void deleteByMemberAndMovie(Member member, Movie movie);
    boolean existsByMemberAndMovie(Member member, Movie movie);

    // Movie 연관관계를 한 번에 패치해서 N+1 방지
    @EntityGraph(attributePaths = {"movie"})
    Page<Bookmark> findByMember_Id(Long memberId, Pageable pageable);
}
