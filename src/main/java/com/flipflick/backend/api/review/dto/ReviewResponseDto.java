package com.flipflick.backend.api.review.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class ReviewResponseDto {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "리뷰 상세 정보")
    public static class Detail {

        @Schema(description = "tmdb ID", example = "1")
        private Long tmdbId;

        @Schema(description = "리뷰 ID", example = "1")
        private Long reviewId;

        @Schema(description = "작성자 ID")
        private Long memberId;

        @Schema(description = "리뷰 내용", example = "정말 재미있는 영화였습니다.")
        private String content;

        @Schema(description = "별점", example = "4.5")
        private Double star;

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

        @Schema(description = "영화 제목")
        private String movieTitle;

        @Schema(description = "포스터 이미지")
        private String posterImg;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "리뷰 페이지 응답")
    public static class PageResponse {
        @Schema(description = "리뷰 목록")
        private List<Detail> reviews;

        @Schema(description = "현재 페이지")
        private int currentPage;

        @Schema(description = "전체 페이지 수")
        private int totalPages;

        @Schema(description = "전체 요소 수")
        private long totalElements;

        @Schema(description = "첫 페이지 여부")
        private boolean first;

        @Schema(description = "마지막 페이지 여부")
        private boolean last;

        @Schema(description = "빈 페이지 여부")
        private boolean empty;

        // 🎯 추가: empty() 정적 메서드
        public static PageResponse empty() {
            return PageResponse.builder()
                    .reviews(Collections.emptyList())
                    .currentPage(0)
                    .totalPages(0)
                    .totalElements(0L)
                    .first(true)
                    .last(true)
                    .empty(true)
                    .build();
        }

        // 🎯 추가: from() 메서드
        public static PageResponse from(Page<Detail> page) {
            return PageResponse.builder()
                    .reviews(page.getContent())
                    .currentPage(page.getNumber())
                    .totalPages(page.getTotalPages())
                    .totalElements(page.getTotalElements())
                    .first(page.isFirst())
                    .last(page.isLast())
                    .empty(page.isEmpty())
                    .build();
        }
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "리뷰 생성 응답")
    public static class Create {
        @Schema(description = "생성된 리뷰 ID", example = "1")
        private Long reviewId;

        @Schema(description = "리뷰 내용", example = "정말 재미있는 영화였습니다.")
        private String content;

        @Schema(description = "별점", example = "4.5")
        private Double star;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "리뷰 수정 응답")
    public static class Update {
        @Schema(description = "수정된 리뷰 ID", example = "1")
        private Long reviewId;

        @Schema(description = "수정된 리뷰 내용", example = "수정된 리뷰 내용입니다.")
        private String content;

        @Schema(description = "수정된 별점", example = "4.0")
        private Double star;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "리뷰 삭제 응답")
    public static class Delete {
        @Schema(description = "삭제된 리뷰 ID", example = "1")
        private Long reviewId;

        @Schema(description = "삭제 메시지", example = "리뷰가 삭제되었습니다.")
        private String message;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "리뷰 좋아요/싫어요 응답")
    public static class LikeHate {
        @Schema(description = "리뷰 ID", example = "1")
        private Long reviewId;

        @Schema(description = "처리 타입", example = "LIKE")
        private String type;

        @Schema(description = "처리 결과", example = "좋아요가 추가되었습니다.")
        private String message;

        @Schema(description = "현재 좋아요 수", example = "16")
        private Long likeCnt;

        @Schema(description = "현재 싫어요 수", example = "2")
        private Long hateCnt;
    }

    // ...existing code...

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "내 리뷰 조회 응답")
    public static class MyReview {

        @Schema(description = "리뷰 존재 여부", example = "true")
        private Boolean hasReview;

        @Schema(description = "리뷰 상세 정보 (없으면 null)")
        private Detail review;

        public static MyReview of(boolean hasReview, Detail review) {
            return MyReview.builder()
                    .hasReview(hasReview)
                    .review(review)
                    .build();
        }
    }
}