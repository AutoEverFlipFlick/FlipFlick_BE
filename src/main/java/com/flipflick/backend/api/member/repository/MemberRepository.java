package com.flipflick.backend.api.member.repository;

import com.flipflick.backend.api.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member,Long> {

    Optional<Member> findByEmail(String email);

    Optional<Member> findBySocialId(String socialId);

    Optional<Member> findByNickname(String nickname);

    // 정지 기간이 만료된 사용자들 조회
    @Query("SELECT m FROM Member m WHERE m.block = 1 AND m.blockDate <= :now")
    List<Member> findExpiredSuspensions(@Param("now") LocalDateTime now);

}
