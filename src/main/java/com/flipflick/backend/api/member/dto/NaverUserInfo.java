package com.flipflick.backend.api.member.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class NaverUserInfo {
    private String id;
    private String name;
}