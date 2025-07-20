package com.flipflick.backend.api.member.dto;

import com.flipflick.backend.api.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberListResponseDto {
    private Long memberId;
    private String email;
    private String nickname;
    private String popcornGrade;
    private LocalDateTime createdAt;
    private String status; // 정상 / 정지 / 차단
    private int reviewCount;
    private int postCount;
    private int warnCount;
    private int blockCount;

    public static MemberListResponseDto from(Member member, int reviewCount, int postCount) {
        return MemberListResponseDto.builder()
                .memberId(member.getId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .popcornGrade(member.getPopcornGrade(member.getPopcorn()))
                .createdAt(member.getCreatedAt())
                .status(getStatusText(member.getBlock()))
                .reviewCount(reviewCount)
                .postCount(postCount)
                .warnCount(member.getWarnCount() != null ? member.getWarnCount() : 0)
                .blockCount(member.getBlockCount() != null ? member.getBlockCount() : 0)
                .build();
    }

    private static String getStatusText(Integer block) {
        if (block == null) return "정상";
        return switch (block) {
            case 1 -> "정지";
            case 2 -> "차단";
            default -> "정상";
        };
    }



}