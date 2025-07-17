package com.flipflick.backend.api.member.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NaverCodeRequestDto {
    private String code;
    private String state;
}