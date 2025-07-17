package com.flipflick.backend.api.playlist.entity;

import com.flipflick.backend.api.member.entity.Member;
import com.flipflick.backend.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "play_list")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlayList extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Boolean hidden = false;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Builder
    public PlayList(String title, Boolean hidden, Member member) {
        this.title = title;
        this.hidden = hidden;
        this.member = member;
    }

    // 플레이리스트 정보 수정
    public void updateInfo(String title, Boolean hidden) {
        this.title = title;
        this.hidden = hidden;
    }

    // 소프트 삭제
    public void softDelete() {
        this.isDeleted = true;
    }

}
