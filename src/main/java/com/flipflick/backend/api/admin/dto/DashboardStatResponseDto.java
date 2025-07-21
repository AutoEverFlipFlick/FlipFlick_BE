package com.flipflick.backend.api.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter

@AllArgsConstructor
public class DashboardStatResponseDto {

    private Map<String, Map<String, List<TimeSeriesData>>> stats;

    public static DashboardStatResponseDto of(
            Map<String, Map<String, List<TimeSeriesData>>> result
    ) {
        return new DashboardStatResponseDto(result);
    }
}