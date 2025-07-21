package com.flipflick.backend.api.movie.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieBWLHListResponseDTO {

    private long totalElements; // 잔체 요소 수
    private int totalPages; // 전체 페이지 수
    private int page; // 현재 페이지(0부터 시작)
    private int size; // 요청한 페이지 사이즈
    private boolean isLast; // 마지막 페이지 여부
    private List<MovieBWLHResponseDTO> content;
}
