package com.flipflick.backend.api.search.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberListResponseDTO {

    private Long memberId;
    private String nickname;
    private long followCnt;
    private boolean followed;
    private String profileImage;
}
