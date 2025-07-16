package com.flipflick.backend.api.movie.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CastResponseDTO {
    private Long id;          // 배우 TMDB ID
    private String name;      // 배우 이름
    private String profileImg; // 프로필 이미지 URL
}
