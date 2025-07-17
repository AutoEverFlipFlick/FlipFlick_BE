package com.flipflick.backend.api.playlist.entity;

import com.flipflick.backend.api.member.entity.Member;
import com.flipflick.backend.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "play_list_bookmark")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlayListBookmark extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "play_list_id", nullable = false)
    private PlayList playList;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Builder
    public PlayListBookmark(PlayList playList, Member member) {
        this.playList = playList;
        this.member = member;
    }
}