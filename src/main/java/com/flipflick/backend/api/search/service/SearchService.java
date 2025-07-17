package com.flipflick.backend.api.search.service;

import com.flipflick.backend.api.search.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
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

        // WebClient로 TMDB 검색 API 호출
        TmdbMovieSearchResponseDTO tmdb = tmdbWebClient.get()
                .uri(uriBuilder -> buildMovieSearchUri(uriBuilder, searchRequestDTO))
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
                    .isLast(true)
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

        boolean isLast = tmdb.getPage() >= tmdb.getTotalPages();

        return MovieListPageResponseDTO.builder()
                .totalElements(tmdb.getTotalResults())
                .totalPages(tmdb.getTotalPages())
                .page(tmdb.getPage())
                .size(content.size())
                .isLast(isLast)
                .content(content)
                .build();
    }

    // 배우 조회
    public CastListPageResponseDTO searchCastList(SearchRequestDTO searchRequestDTO) {
        // TMDB person 검색 호출
        TmdbPersonSearchResponseDTO tmdb = tmdbWebClient.get()
                .uri(builder -> buildCastSearchUri(builder, searchRequestDTO))
                .retrieve()
                .bodyToMono(TmdbPersonSearchResponseDTO.class)
                .block();

        // null 혹은 빈 결과 처리
        if (tmdb == null || tmdb.getResults() == null) {
            return CastListPageResponseDTO.builder()
                    .totalElements(0)
                    .totalPages(0)
                    .page(searchRequestDTO.getPage())
                    .size(0)
                    .isLast(true)
                    .content(Collections.emptyList())
                    .build();
        }

        // id, name, profile, known_for 중 이름만 추출
        List<CastListResponseDTO> content = tmdb.getResults().stream()
                .map(this::toCastDto)
                .collect(Collectors.toList());

        boolean isLast = tmdb.getPage() >= tmdb.getTotalPages();

        return CastListPageResponseDTO.builder()
                .totalElements(tmdb.getTotalResults())
                .totalPages(tmdb.getTotalPages())
                .page(tmdb.getPage())
                .size(content.size())
                .isLast(isLast)
                .content(content)
                .build();
    }

    private CastListResponseDTO toCastDto(TmdbPersonSearchResponseDTO.Person p) {
        // known_for 배열에서 title 또는 name 중 non-null 값만 뽑아서 리스트로
        List<String> knownForNames = p.getKnownFor().stream()
                .map(kf -> kf.getTitle() != null ? kf.getTitle() : kf.getName())
                .collect(Collectors.toList());

        return CastListResponseDTO.builder()
                .tmdbId(p.getId())
                .name(p.getName())
                .profileImage(
                        p.getProfilePath() != null
                                ? imageBaseUrl + p.getProfilePath()
                                : null
                )
                .knownFor(knownForNames)
                .build();
    }

    // 배우 검색 URI
    private URI buildCastSearchUri(UriBuilder uriBuilder, SearchRequestDTO searchRequestDTO) {
        return uriBuilder
                .path("/search/person")
                .queryParam("api_key", apiKey)
                .queryParam("language", "ko-KR")
                .queryParam("query", searchRequestDTO.getQuery())
                .queryParam("page", searchRequestDTO.getPage())
                .build();
    }

    // 영화 검색 URI
    private URI buildMovieSearchUri(UriBuilder uriBuilder, SearchRequestDTO searchRequestDTO) {
        return uriBuilder
                .path("/search/movie")
                .queryParam("api_key", apiKey)
                .queryParam("language", "ko-KR")
                .queryParam("query", searchRequestDTO.getQuery())
                .queryParam("page", searchRequestDTO.getPage())
                .build();
    }
}
