package com.flipflick.backend.api.member.repository;

import com.flipflick.backend.api.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member,Long> {

    Optional<Member> findByIdAndIsDeletedFalse(Long id);

    Optional<Member> findByEmailAndIsDeletedFalse(String email);

    Optional<Member> findBySocialIdAndIsDeletedFalse(String socialId);

    Optional<Member> findByNicknameAndIsDeletedFalse(String nickname);

    // 정지 기간이 만료된 사용자들 조회
    @Query("SELECT m FROM Member m WHERE m.block = 1 AND m.blockDate <= :now")
    List<Member> findExpiredSuspensions(@Param("now") LocalDateTime now);
    boolean existsByEmailAndIsDeletedFalse(String email);
    boolean existsByNicknameAndIsDeletedFalse(String nickname);
    boolean existsByIdAndIsDeletedFalse(Long id);
    Page<Member> findByNicknameContainingAndIsDeletedFalse(String keyword, Pageable pageable);

    @Query(value = """
    SELECT CAST(m.created_at AS DATE) AS date, COUNT(*) AS count
    FROM member m
    WHERE CAST(m.created_at AS DATE) <= :endDate
    GROUP BY CAST(m.created_at AS DATE)
    ORDER BY CAST(m.created_at AS DATE)
""", nativeQuery = true)
    List<Object[]> countMembersUntilDate(@Param("endDate") LocalDate endDate);

    @Query(value = """
    SELECT CAST(m.created_at AS DATE) AS date, COUNT(*) AS count
    FROM member m
    WHERE CAST(m.created_at AS DATE) BETWEEN :startDate AND :endDate
    GROUP BY CAST(m.created_at AS DATE)
    ORDER BY CAST(m.created_at AS DATE)
""", nativeQuery = true)
    List<Object[]> countNewMembersByDate(@Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate);

    @Query("""
        SELECT 
            CASE
                WHEN m.popcorn >= 81 THEN '팝콘기계'
                WHEN m.popcorn >= 71 THEN '1 팝콘'
                WHEN m.popcorn >= 61 THEN '2/3 팝콘'
                WHEN m.popcorn >= 51 THEN '1/3 팝콘'
                WHEN m.popcorn >= 41 THEN '빈 팝콘'
                WHEN m.popcorn >= 31 THEN '옥수수 3'
                WHEN m.popcorn >= 21 THEN '옥수수 2'
                ELSE '옥수수 1'
            END AS grade,
            COUNT(m) AS count
        FROM Member m
        GROUP BY grade
        ORDER BY count DESC
    """)
    List<Object[]> countMembersByPopcornGrade();

    Page<Member> findByNicknameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String nicknameKeyword,
            String emailKeyword,
            Pageable pageable
    );

}
