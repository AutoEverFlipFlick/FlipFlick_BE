package com.flipflick.backend.api.debate.repository;

import com.flipflick.backend.api.debate.entity.Debate;
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
public interface DebateRepository extends JpaRepository<Debate, Long> {

    // 특정 영화의 토론 조회 (삭제되지 않은 것만, 최신순)
    @Query("SELECT d FROM Debate d " +
            "WHERE d.movie.tmdbId = :tmdbId AND d.isDeleted = false " +
            "ORDER BY d.createdAt DESC")
    Page<Debate> findByMovieTmdbIdAndIsDeletedFalseOrderByCreatedAtDesc(@Param("tmdbId") Long tmdbId, Pageable pageable);

    // 특정 영화의 토론 조회 (삭제되지 않은 것만, 인기순)
    @Query("SELECT d FROM Debate d " +
            "WHERE d.movie.tmdbId = :tmdbId AND d.isDeleted = false " +
            "ORDER BY d.likeCnt DESC, d.createdAt DESC")
    Page<Debate> findByMovieTmdbIdAndIsDeletedFalseOrderByLikeCntDesc(@Param("tmdbId") Long tmdbId, Pageable pageable);

    // 특정 사용자와 영화의 토론 조회 (삭제되지 않은 것만, 최신순)
    @Query("SELECT d FROM Debate d " +
            "WHERE d.member.id = :memberId AND d.movie.tmdbId = :tmdbId AND d.isDeleted = false " +
            "ORDER BY d.createdAt DESC")
    Page<Debate> findByMemberIdAndMovieTmdbIdAndIsDeletedFalseOrderByCreatedAtDesc(@Param("memberId") Long memberId, @Param("tmdbId") Long tmdbId, Pageable pageable);

    // 특정 사용자와 영화의 토론 조회 (삭제되지 않은 것만, 인기순)
    @Query("SELECT d FROM Debate d " +
            "WHERE d.member.id = :memberId AND d.movie.tmdbId = :tmdbId AND d.isDeleted = false " +
            "ORDER BY d.likeCnt DESC, d.createdAt DESC")
    Page<Debate> findByMemberIdAndMovieTmdbIdAndIsDeletedFalseOrderByLikeCntDesc(@Param("memberId") Long memberId, @Param("tmdbId") Long tmdbId, Pageable pageable);

    // 특정 토론 조회 (삭제되지 않은 것만)
    @Query("SELECT d FROM Debate d WHERE d.id = :debateId AND d.isDeleted = false")
    Optional<Debate> findByIdAndIsDeletedFalse(@Param("debateId") Long debateId);

    // 닉네임으로 토론 조회 (최신순)
    @Query("SELECT d FROM Debate d " +
            "WHERE d.member.nickname = :nickname AND d.isDeleted = false " +
            "ORDER BY d.createdAt DESC")
    Page<Debate> findByMemberNicknameAndIsDeletedFalseOrderByCreatedAtDesc(@Param("nickname") String nickname, Pageable pageable);

    // 닉네임으로 토론 조회 (인기순)
    @Query("SELECT d FROM Debate d " +
            "WHERE d.member.nickname = :nickname AND d.isDeleted = false " +
            "ORDER BY d.likeCnt DESC, d.createdAt DESC")
    Page<Debate> findByMemberNicknameAndIsDeletedFalseOrderByLikeCntDesc(@Param("nickname") String nickname, Pageable pageable);

    // 특정 사용자의 토론 조회 (삭제되지 않은 것만, 최신순)
    @Query("SELECT d FROM Debate d " +
            "WHERE d.member.id = :memberId AND d.isDeleted = false " +
            "ORDER BY d.createdAt DESC")
    Page<Debate> findByMemberIdAndIsDeletedFalseOrderByCreatedAtDesc(@Param("memberId") Long memberId, Pageable pageable);

    // 특정 사용자의 토론 조회 (삭제되지 않은 것만, 인기순)
    @Query("SELECT d FROM Debate d " +
            "WHERE d.member.id = :memberId AND d.isDeleted = false " +
            "ORDER BY d.likeCnt DESC, d.createdAt DESC")
    Page<Debate> findByMemberIdAndIsDeletedFalseOrderByLikeCntDesc(@Param("memberId") Long memberId, Pageable pageable);

    // 날짜별 전체 토론 수
    @Query(value = """
    SELECT CAST(d.created_at AS DATE) AS date, COUNT(*) AS count
    FROM debate d
    WHERE CAST(d.created_at AS DATE) <= :endDate AND d.is_deleted = false
    GROUP BY CAST(d.created_at AS DATE)
    ORDER BY CAST(d.created_at AS DATE)
""", nativeQuery = true)
    List<Object[]> countDebatesUntilDate(@Param("endDate") LocalDate endDate);

    // 날짜별 신규 토론 수
    @Query(value = """
    SELECT CAST(d.created_at AS DATE) AS date, COUNT(*) AS count
    FROM debate d
    WHERE CAST(d.created_at AS DATE) BETWEEN :startDate AND :endDate AND d.is_deleted = false
    GROUP BY CAST(d.created_at AS DATE)
    ORDER BY CAST(d.created_at AS DATE)
""", nativeQuery = true)
    List<Object[]> countNewDebatesByDate(@Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate);
}
