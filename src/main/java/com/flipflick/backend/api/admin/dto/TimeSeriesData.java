package com.flipflick.backend.api.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class TimeSeriesData {
    private LocalDate date;
    private Long newCount;
    private Long totalCount;
}