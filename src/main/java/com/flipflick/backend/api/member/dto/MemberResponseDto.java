package com.flipflick.backend.api.member.dto;


import com.flipflick.backend.api.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.*;


@Getter @AllArgsConstructor @NoArgsConstructor @Builder
public class MemberResponseDto {
    private Long   id;
    private String email;
    private String nickname;
    // 필요에 따라 추가 필드 예: private String profileImageUrl;

    public static MemberResponseDto of(Member member) {
        return MemberResponseDto.builder()
                .id(member.getId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .build();
    }
}
