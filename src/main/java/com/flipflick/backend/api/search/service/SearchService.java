package com.flipflick.backend.api.search.service;

import com.flipflick.backend.api.movie.dto.MovieListPageResponseDTO;
import com.flipflick.backend.api.movie.dto.MovieListResponseDTO;
import com.flipflick.backend.api.search.dto.TmdbMovieSearchResponseDTO;
import com.flipflick.backend.api.search.dto.SearchRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SearchService {

    @Value("${tmdb.api.key}")
    private String apiKey;

    @Value("${tmdb.api.image-base-url}")
    private String imageBaseUrl;

    private final WebClient tmdbWebClient;

    public SearchService(WebClient tmdbWebClient) {
        this.tmdbWebClient = tmdbWebClient;
    }

    public MovieListPageResponseDTO searchMovieList(SearchRequestDTO searchRequestDTO) {

        // 1) WebClient로 TMDB 검색 API 호출
        TmdbMovieSearchResponseDTO tmdb = tmdbWebClient.get()
                .uri(uriBuilder -> buildSearchUri(uriBuilder, searchRequestDTO))
                .retrieve()
                .bodyToMono(TmdbMovieSearchResponseDTO.class)
                .block();

        // null/빈 결과 처리
        if (tmdb == null || tmdb.getResults() == null) {
            return MovieListPageResponseDTO.builder()
                    .totalElements(0)
                    .totalPages(0)
                    .page(searchRequestDTO.getPage())
                    .size(0)
                    .content(Collections.emptyList())
                    .build();
        }

        // 개봉일자 최신순으로 정렬
        List<MovieListResponseDTO> content = tmdb.getResults().stream()
                .sorted(Comparator.comparing(
                        TmdbMovieSearchResponseDTO.TmdbMovie::getReleaseDate,
                        Comparator.nullsLast(Comparator.naturalOrder())
                    )
                        .reversed()
                )
                .map(m -> MovieListResponseDTO.builder()
                        .tmdbId(m.getTmdbId())
                        .title(m.getTitle())
                        .releaseDate(m.getReleaseDate())
                        .image(m.getImagePath() != null
                                ? imageBaseUrl + m.getImagePath()
                                : null)
                        .build())
                .collect(Collectors.toList());

        return MovieListPageResponseDTO.builder()
                .totalElements(tmdb.getTotalResults())
                .totalPages(tmdb.getTotalPages())
                .page(tmdb.getPage())
                .size(content.size())
                .content(content)
                .build();
    }

    // URI 빌더 메서드
    private java.net.URI buildSearchUri(UriBuilder uriBuilder, SearchRequestDTO dto) {
        return uriBuilder
                .path("/search/movie")
                .queryParam("api_key", apiKey)
                .queryParam("language", "ko-KR")
                .queryParam("query", dto.getQuery())
                .queryParam("page", dto.getPage())
                .build();
    }
}
