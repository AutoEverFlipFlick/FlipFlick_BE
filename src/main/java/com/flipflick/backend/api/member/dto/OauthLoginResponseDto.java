package com.flipflick.backend.api.member.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OauthLoginResponseDto {

    private String accessToken;
    private String refreshToken;

    @JsonProperty("isNew")
    private boolean isNew; // 최초 가입 여부
}
