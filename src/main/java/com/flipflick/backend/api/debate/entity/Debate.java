package com.flipflick.backend.api.debate.entity;

import com.flipflick.backend.api.member.entity.Member;
import com.flipflick.backend.api.movie.entity.Movie;
import com.flipflick.backend.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "debate")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Debate extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "debate_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @Lob
    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private Boolean spoiler;

    @Column(name = "like_cnt")
    @Builder.Default
    private Long likeCnt = 0L;

    @Column(name = "hate_cnt")
    @Builder.Default
    private Long hateCnt = 0L;

    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean isDeleted = false;

    // 토론 수정
    public void updateDebate(String content, Boolean spoiler) {
        this.content = content;
        this.spoiler = spoiler;
    }

    // 좋아요 수 증가/감소
    public void increaseLikeCnt() {
        this.likeCnt++;
    }

    public void decreaseLikeCnt() {
        this.likeCnt--;
    }

    // 싫어요 수 증가/감소
    public void increaseHateCnt() {
        this.hateCnt++;
    }

    public void decreaseHateCnt() {
        this.hateCnt--;
    }

    // 소프트 삭제
    public void softDelete() {
        this.isDeleted = true;
    }
}
