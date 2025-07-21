package com.flipflick.backend.api.recommendation.entity;

import com.flipflick.backend.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_similarity")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSimilarity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "similar_member_id", nullable = false)
    private Long similarMemberId;

    @Column(name = "similarity_score", nullable = false)
    private Double similarityScore;

    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt;

}