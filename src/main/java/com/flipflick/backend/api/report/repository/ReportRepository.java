package com.flipflick.backend.api.report.repository;

import com.flipflick.backend.api.report.entity.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReportRepository extends JpaRepository<Report,Long> {

    @Query("""
    SELECT r FROM Report r
    JOIN FETCH r.reporter
    JOIN FETCH r.target
    WHERE (
        :keyword IS NULL OR
        LOWER(r.reporter.nickname) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
        LOWER(r.target.nickname) LIKE LOWER(CONCAT('%', :keyword, '%'))
    )
    AND (
        :status = '전체' OR
        (:status = '처리' AND r.handled = true) OR
        (:status = '미처리' AND r.handled = false)
    )
""")
    Page<Report> findReportsFiltered(
            @Param("keyword") String keyword,
            @Param("status") String status,
            Pageable pageable
    );

}
