package com.flipflick.backend.api.search.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class TmdbMovieSearchResponseDTO {

    private int page;

    @JsonProperty("total_results")
    private long totalResults;

    @JsonProperty("total_pages")
    private int totalPages;

    private List<TmdbMovie> results;

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Builder
    public static class TmdbMovie {
        @JsonProperty("id")
        private Long tmdbId;

        private String title;

        @JsonProperty("release_date")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private LocalDate releaseDate;

        @JsonProperty("poster_path")
        private String imagePath;
    }
}
