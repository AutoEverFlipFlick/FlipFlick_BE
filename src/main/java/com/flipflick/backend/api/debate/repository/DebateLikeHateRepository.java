package com.flipflick.backend.api.debate.repository;

import com.flipflick.backend.api.debate.entity.DebateLikeHate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface DebateLikeHateRepository extends JpaRepository<DebateLikeHate, Long> {

    // 특정 사용자의 특정 토론에 대한 좋아요/싫어요 조회
    @Query("SELECT dlh FROM DebateLikeHate dlh " +
            "WHERE dlh.debate.id = :debateId AND dlh.member.id = :memberId")
    Optional<DebateLikeHate> findByDebateIdAndMemberId(@Param("debateId") Long debateId, @Param("memberId") Long memberId);

    // 특정 토론의 좋아요 수
    @Query("SELECT COUNT(dlh) FROM DebateLikeHate dlh " +
            "WHERE dlh.debate.id = :debateId AND dlh.type = 'LIKE'")
    Long countLikesByDebateId(@Param("debateId") Long debateId);

    // 특정 토론의 싫어요 수
    @Query("SELECT COUNT(dlh) FROM DebateLikeHate dlh " +
            "WHERE dlh.debate.id = :debateId AND dlh.type = 'HATE'")
    Long countHatesByDebateId(@Param("debateId") Long debateId);


    // 수정: 특정 날짜 끝 시점의 좋아요 수 계산
    @Query("SELECT COUNT(dlh) FROM DebateLikeHate dlh " +
            "WHERE dlh.debate.member.id = :memberId AND dlh.type = 'LIKE' " +
            "AND dlh.createdAt < :endOfDay")
    Integer countLikesByMemberIdUntilDate(@Param("memberId") Long memberId,
                                          @Param("endOfDay") LocalDateTime endOfDay);

    // 수정: 특정 날짜 끝 시점의 싫어요 수 계산
    @Query("SELECT COUNT(dlh) FROM DebateLikeHate dlh " +
            "WHERE dlh.debate.member.id = :memberId AND dlh.type = 'HATE' " +
            "AND dlh.createdAt < :endOfDay")
    Integer countHatesByMemberIdUntilDate(@Param("memberId") Long memberId,
                                          @Param("endOfDay") LocalDateTime endOfDay);
}
