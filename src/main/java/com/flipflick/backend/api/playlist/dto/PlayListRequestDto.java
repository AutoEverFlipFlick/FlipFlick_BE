package com.flipflick.backend.api.playlist.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

public class PlayListRequestDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Create {
        private String title;
        private Boolean hidden;
        private java.util.List<MovieInfo> movies;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MovieInfo {
        private Integer tmdbId;
        private String posterUrl;
        private String title;
        private LocalDate releaseDate;  // releaseDate 필드 추가
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Bookmark {
        private Long playListId;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class List {
        private String sortBy = "popularity";
        private Long userId;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Update {
        private String title;
        private Boolean hidden;
        private java.util.List<MovieInfo> movies;

        // setter 메서드 추가
        public void setMovies(java.util.List<MovieInfo> movies) {
            this.movies = movies;
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Delete {
        private Long playListId;
    }
}
