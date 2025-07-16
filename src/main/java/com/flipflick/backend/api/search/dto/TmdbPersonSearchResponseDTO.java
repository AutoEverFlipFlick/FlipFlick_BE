package com.flipflick.backend.api.search.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TmdbPersonSearchResponseDTO {

    private int page;
    private List<Person> results;
    @JsonProperty("total_pages")
    private int totalPages;
    @JsonProperty("total_results")
    private long totalResults;

    @Getter
    @Builder
    public static class Person {
        private long id;
        private String name;
        @JsonProperty("profile_path")
        private String profilePath;
        @JsonProperty("known_for")
        private List<KnownFor> knownFor;
    }

    @Getter
    @Builder
    public static class KnownFor {
        // 영화의 경우 title, TV의 경우 name 필드에 이름이 들어옴
        private String title;
        private String name;
    }
}