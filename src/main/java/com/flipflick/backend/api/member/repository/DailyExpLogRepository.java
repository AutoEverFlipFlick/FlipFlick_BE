package com.flipflick.backend.api.member.repository;

import com.flipflick.backend.api.member.entity.DailyExpLog;
import com.flipflick.backend.api.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyExpLogRepository extends JpaRepository<DailyExpLog, Long> {

    // 특정 날짜의 미처리된 로그 조회
    @Query("SELECT d FROM DailyExpLog d WHERE d.logDate = :date AND d.isProcessed = false")
    List<DailyExpLog> findUnprocessedByDate(@Param("date") LocalDate date);

    // 특정 사용자의 최근 로그 조회
    @Query("SELECT d FROM DailyExpLog d WHERE d.member.id = :memberId AND d.logDate >= :startDate ORDER BY d.logDate DESC")
    List<DailyExpLog> findRecentLogsByMemberId(@Param("memberId") Long memberId, @Param("startDate") LocalDate startDate);

    // 특정 사용자의 특정 날짜 로그 조회
    Optional<DailyExpLog> findByMemberAndLogDate(Member member, LocalDate date);

    // 특정 날짜의 모든 로그 조회 (테스트용)
    @Query("SELECT d FROM DailyExpLog d WHERE d.logDate = :date")
    List<DailyExpLog> findByLogDate(@Param("date") LocalDate date);

}