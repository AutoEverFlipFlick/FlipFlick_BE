package com.flipflick.backend.api.playlist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;

public class PlayListResponseDto {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private Long playListId;
        private String title;
        private String nickname;
        private String thumbnailUrl;
        private Integer movieCount;
        private Integer bookmarkCount;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlaylistDetail {
        private Long playListId;
        private String title;
        private String nickname;
        private boolean hidden;
        private Integer movieCount;
        private Integer bookmarkCount;
        private MoviePageResponse movies;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MovieInfo {
        private Long movieId;
        private String title;
        private LocalDate releaseDate;
        private String posterUrl;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlaylistCreate {
        private Long playListId;
        private String title;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PlaylistUpdate {
        private Long playListId;
        private String title;
        private Integer addedMovieCount;
        private Integer removedMovieCount;
        private Integer totalMovieCount;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PlaylistDelete {
        private String title;
    }

    // 페이지네이션 응답 클래스
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PlaylistPageResponse {
        private List<Summary> content;
        private int currentPage;
        private int totalPages;
        private long totalElements;
        private int numberOfElements;
        private boolean first;
        private boolean last;

        // Page 객체에서 변환하는 정적 메서드
        public static PlaylistPageResponse from(Page<Summary> page) {
            return PlaylistPageResponse.builder()
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

    // 영화 목록 페이지네이션 응답 클래스 추가
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MoviePageResponse {
        private List<MovieInfo> content;
        private int currentPage;
        private int totalPages;
        private long totalElements;
        private int numberOfElements;
        private boolean first;
        private boolean last;

        // Page 객체에서 변환하는 정적 메서드
        public static MoviePageResponse from(Page<MovieInfo> page) {
            return MoviePageResponse.builder()
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
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BookmarkIds {
        private List<Long> playListIds;
        private int totalCount;
    }
}
