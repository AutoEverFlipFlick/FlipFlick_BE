package com.flipflick.backend.api.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MovieReviewCountResponseDto {
    private String title;
    private Long reviewCount;
}

