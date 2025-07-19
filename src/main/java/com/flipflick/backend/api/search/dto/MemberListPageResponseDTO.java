package com.flipflick.backend.api.search.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MemberListPageResponseDTO {

    private long totalElements; // 잔체 요소 수
    private int totalPages; // 전체 페이지 수
    private int page; // 현재 페이지(0부터 시작)
    private int size; // 요청한 페이지 사이즈
    private boolean isLast; // 마지막 페이지 여부
    private List<MemberListResponseDTO> content;
}
