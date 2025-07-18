package com.flipflick.backend.api.member.entity;

import com.flipflick.backend.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "daily_exp_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DailyExpLog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    @Column(name = "follow_count")
    @Builder.Default
    private Integer followCount = 0;

    @Column(name = "review_like_count")
    @Builder.Default
    private Integer reviewLikeCount = 0;

    @Column(name = "review_hate_count")
    @Builder.Default
    private Integer reviewHateCount = 0;

    @Column(name = "daily_exp")
    @Builder.Default
    private Double dailyExp = 0.0;

    @Column(name = "is_processed")
    @Builder.Default
    private Boolean isProcessed = false;

    // 일일 경험치 계산
    public void calculateDailyExp() {
        this.dailyExp = (followCount * 3.0) + (reviewLikeCount * 2.0) - (reviewHateCount * 1.0);
    }

    // 처리 완료 표시
    public void markAsProcessed() {
        this.isProcessed = true;
    }

    // 수치 업데이트
    public void updateCounts(Integer followCount, Integer reviewLikeCount, Integer reviewHateCount) {
        this.followCount = followCount;
        this.reviewLikeCount = reviewLikeCount;
        this.reviewHateCount = reviewHateCount;
        calculateDailyExp();
    }

    public void setProcessed(boolean b) {
        this.isProcessed = b;
    }
}