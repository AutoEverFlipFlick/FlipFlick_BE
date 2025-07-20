package com.flipflick.backend.api.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReportActionRequestDto {
    private String action; // 경고, 정지, 차단, 기각
}
