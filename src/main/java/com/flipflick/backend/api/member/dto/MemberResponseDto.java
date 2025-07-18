package com.flipflick.backend.api.member.dto;


import com.flipflick.backend.api.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.*;


@Getter @AllArgsConstructor @NoArgsConstructor @Builder
public class MemberResponseDto {
    private Long id;
    private String email;
    private String nickname;
    private String profileImage;
    private long followerCount;
    private long followingCount;

    public static MemberResponseDto of(Member member) {
        return MemberResponseDto.builder()
                .id(member.getId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .profileImage(member.getProfileImage())
                .followerCount(member.getFollowers().size())     // ← 여기도 연관관계 필요
                .followingCount(member.getFollowings().size())   // ← 여기도 연관관계 필요
                .build();
    }
}
