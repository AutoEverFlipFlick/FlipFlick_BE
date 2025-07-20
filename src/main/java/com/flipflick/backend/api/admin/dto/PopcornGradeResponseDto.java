package com.flipflick.backend.api.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PopcornGradeResponseDto {
    private String grade;
    private long count;
}
