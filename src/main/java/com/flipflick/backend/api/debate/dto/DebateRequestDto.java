package com.flipflick.backend.api.debate.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class DebateRequestDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "토론 작성 요청")
    public static class DebateCreate {
        @NotNull(message = "영화 ID는 필수입니다.")
        @Schema(description = "영화 TMDB ID", example = "550")
        private Long tmdbId;

        @NotBlank(message = "토론 제목은 필수입니다.")
        @Size(max = 100, message = "토론 제목은 100자 이하로 입력해주세요.")
        @Schema(description = "토론 제목", example = "이 영화의 엔딩에 대한 해석")
        private String debateTitle;

        @NotBlank(message = "토론 내용은 필수입니다.")
        @Size(min = 10, max = 1000, message = "토론는 10자 이상 1000자 이하로 작성해주세요.")
        @Schema(description = "토론 내용", example = "정말 재미있는 영화였습니다.")
        private String content;

        @NotNull(message = "스포일러 여부는 필수입니다.")
        @Schema(description = "스포일러 포함 여부", example = "false")
        private Boolean spoiler;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "토론 수정 요청")
    public static class DebateUpdate {
        @NotBlank(message = "토론 제목은 필수입니다.")
        @Size(max = 100, message = "토론 제목은 100자 이하로 입력해주세요.")
        @Schema(description = "토론 제목", example = "수정된 토론 제목")
        private String debateTitle;

        @NotBlank(message = "토론 내용은 필수입니다.")
        @Size(min = 10, max = 1000, message = "토론는 10자 이상 1000자 이하로 작성해주세요.")
        @Schema(description = "토론 내용", example = "수정된 토론 내용입니다.")
        private String content;

        @NotNull(message = "스포일러 여부는 필수입니다.")
        @Schema(description = "스포일러 포함 여부", example = "false")
        private Boolean spoiler;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "토론 좋아요/싫어요 요청")
    public static class DebateLikeHate {
        @NotNull(message = "토론 ID는 필수입니다.")
        @Schema(description = "토론 ID", example = "1")
        private Long debateId;

        @NotNull(message = "좋아요/싫어요 타입은 필수입니다.")
        @Schema(description = "좋아요/싫어요 타입", example = "LIKE", allowableValues = {"LIKE", "HATE"})
        private String type; // "LIKE" or "HATE"
    }
}
