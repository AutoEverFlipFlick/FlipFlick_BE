package com.flipflick.backend.api.member.dto;

import lombok.Getter;

@Getter
public class NaverUserResponse {
    private String resultcode;
    private String message;
    private NaverUserInfo response;
}