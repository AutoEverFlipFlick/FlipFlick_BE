package com.flipflick.backend.api.report.entity;

import com.flipflick.backend.api.member.entity.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "report")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Report {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id")
    private Member reporter; // 신고자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_id")
    private Member target; // 피신고자

    private String type; // 예: 리뷰, 토론
    private String content; // 신고 사유

    private String targetTitle; // 신고된 글 제목

    @Lob
    private String targetContent; // 신고된 글 내용

    private Long targetEntityId; // 해당되는 신고의 entityId



    @Column(name = "handled")
    @Builder.Default
    private boolean handled = false; // 처리 여부

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public void markAsHandled() {
        this.handled = true;
    }

}


