package com.flipflick.backend.api.debate.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class DebateResponseDto {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "토론 상세 정보")
    public static class DebateDetail {
        @Schema(description = "토론 ID", example = "1")
        private Long debateId;

        @Schema(description = "작성자 ID")
        private Long memberId;

        @Schema(description = "tmdbID")
        private Long tmdbId;

        @Schema(description = "영화 상세 정보")
        private DebateMovieInfo movie;

        @Schema(description = "영화 제목")
        private String movieTitle;

        @Schema(description = "토론 제목")
        private String debateTitle;

        @Schema(description = "토론 내용", example = "정말 재미있는 영화였습니다.")
        private String content;

        @Schema(description = "스포일러 포함 여부", example = "false")
        private Boolean spoiler;

        @Schema(description = "좋아요 수", example = "15")
        private Long likeCnt;

        @Schema(description = "싫어요 수", example = "2")
        private Long hateCnt;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(description = "생성일시", example = "2024-01-15 14:30:00")
        private LocalDateTime createdAt;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(description = "수정일시", example = "2024-01-15 14:30:00")
        private LocalDateTime updatedAt;

        // 작성자 정보
        @Schema(description = "작성자 닉네임", example = "영화매니아")
        private String nickname;

        @Schema(description = "작성자 프로필 이미지", example = "https://example.com/profile.jpg")
        private String profileImage;

        @Schema(description = "작성자 팝콘 지수", example = "85.5")
        private Double popcorn;

        @Schema(description = "댓글 수", example = "5")
        private int commentCount;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "토론 목록 페이지네이션 응답")
    public static class DebatePageResponse {
        @Schema(description = "토론 목록")
        private List<DebateDetail> content;

        @Schema(description = "현재 페이지", example = "0")
        private int currentPage;

        @Schema(description = "전체 페이지 수", example = "10")
        private int totalPages;

        @Schema(description = "전체 요소 수", example = "95")
        private long totalElements;

        @Schema(description = "현재 페이지 요소 수", example = "10")
        private int numberOfElements;

        @Schema(description = "첫 페이지 여부", example = "true")
        private boolean first;

        @Schema(description = "마지막 페이지 여부", example = "false")
        private boolean last;

        public static DebatePageResponse from(Page<DebateDetail> page) {
            return DebatePageResponse.builder()
                    .content(page.getContent())
                    .currentPage(page.getNumber())
                    .totalPages(page.getTotalPages())
                    .totalElements(page.getTotalElements())
                    .numberOfElements(page.getNumberOfElements())
                    .first(page.isFirst())
                    .last(page.isLast())
                    .build();
        }
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "토론 생성 응답")
    public static class DebateCreate {
        @Schema(description = "생성된 토론 ID", example = "1")
        private Long debateId;

        @Schema(description = "토론 제목")
        private String debateTitle;

        @Schema(description = "토론 내용", example = "정말 재미있는 영화였습니다.")
        private String content;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "토론 수정 응답")
    public static class DebateUpdate {
        @Schema(description = "수정된 토론 ID", example = "1")
        private Long debateId;

        @Schema(description = "토론 제목")
        private String debateTitle;

        @Schema(description = "수정된 토론 내용", example = "수정된 토론 내용입니다.")
        private String content;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "토론 삭제 응답")
    public static class DebateDelete {
        @Schema(description = "삭제된 토론 ID", example = "1")
        private Long debateId;

        @Schema(description = "삭제 메시지", example = "토론가 삭제되었습니다.")
        private String message;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "토론 좋아요/싫어요 응답")
    public static class DebateLikeHate {
        @Schema(description = "토론 ID", example = "1")
        private Long debateId;

        @Schema(description = "처리 타입", example = "LIKE")
        private String type;

        @Schema(description = "처리 결과", example = "좋아요가 추가되었습니다.")
        private String message;

        @Schema(description = "현재 좋아요 수", example = "16")
        private Long likeCnt;

        @Schema(description = "현재 싫어요 수", example = "2")
        private Long hateCnt;
    }
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "영화 정보")
    public static class DebateMovieInfo {
        @Schema(description = "영화 TMDB ID")
        private Long tmdbId;

        @Schema(description = "영화 제목")
        private String title;


        @Schema(description = "영화 오버뷰")
        private String overview;

        @Schema(description = "영화 포스터 이미지")
        private String posterImg;

        @Schema(description = "영화 개봉일")
        private LocalDate releaseDate;

        @Schema(description = "영화 평점")
        private Double rating;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "사용자의 토론 반응 상태")
    public static class UserReaction {
        @Schema(description = "현재 사용자가 좋아요를 눌렀는지", example = "true")
        private Boolean isLiked;

        @Schema(description = "현재 사용자가 싫어요를 눌렀는지", example = "false")
        private Boolean isHated;
    }
}
