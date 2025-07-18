package com.flipflick.backend.api.follow.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FollowRequestDto {
    private Long targetMemberId; // 팔로우/언팔로우 대상 ID
}