package com.flipflick.backend.api.movie.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.flipflick.backend.api.movie.dto.*;
import com.flipflick.backend.api.movie.entity.*;
import com.flipflick.backend.api.movie.repository.*;
import com.flipflick.backend.common.exception.BadRequestException;
import com.flipflick.backend.common.exception.InternalServerException;
import com.flipflick.backend.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovieService {

    @Value("${tmdb.api.key}")
    private String apiKey;

    @Value("${tmdb.api.image-base-url}")
    private String imageBaseUrl;

    private final WebClient tmdbWebClient;
    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final ProviderRepository providerRepository;

    // 영화 상세 조회 메서드(DB에 영화데이터가 없으면 TMDB호출 및 저장후 반환)
    @Transactional
    public MovieDetailResponseDTO viewMovieDetail(SearchRequestIdDTO dto) {
        Long tmdbId = dto.getTmdbId();

        // 페치 조인으로 genre, media, provider 모두 미리 가져옴
        Movie movie = movieRepository.findWithAllByTmdbId(tmdbId)
                .orElseGet(() -> fetchAndSaveMovie(tmdbId));

        List<CastResponseDTO> casts = fetchCasts(tmdbId);

        return MovieDetailResponseDTO.builder()
                .tmdbId(movie.getTmdbId())
                .title(movie.getTitle())
                .originalTitle(movie.getOriginalTitle())
                .overview(movie.getOverview())
                .posterImg(movie.getPosterImg())
                .backgroundImg(movie.getBackgroundImg())
                .voteAverage(movie.getVoteAverage())
                .popcorn(movie.getPopcorn())
                .releaseDate(movie.getReleaseDate())
                .productionYear(movie.getProductionYear())
                .productionCountry(movie.getProductionCountry())
                .ageRating(movie.getAgeRating())
                .runtime(movie.getRuntime())
                .genres(movie.getMovieGenres().stream()
                        .map(mg -> GenreDTO.builder()
                                .tmdbId(mg.getGenre().getTmdbId())
                                .genreName(mg.getGenre().getGenreName())
                                .build())
                        .collect(Collectors.toList()))
                .images(movie.getMedia().stream()
                        .filter(mv -> mv.getMovieMediaType() == MovieMediaType.IMAGE)
                        .map(MovieImageVideo::getUrl)
                        .collect(Collectors.toList()))
                .videos(movie.getMedia().stream()
                        .filter(mv -> mv.getMovieMediaType() == MovieMediaType.VIDEO)
                        .map(MovieImageVideo::getUrl)
                        .collect(Collectors.toList()))
                .providers(movie.getProviders().stream()
                        .map(mp -> ProviderDTO.builder()
                                .providerName(mp.getProvider().getProviderName())
                                .providerType(mp.getProviderType().name())
                                .build())
                        .collect(Collectors.toList()))
                .casts(casts)
                .build();
    }

    // TMDB API 호출 및 DB 저장
    private Movie fetchAndSaveMovie(Long tmdbId) {
        JsonNode root = tmdbWebClient.get()
                .uri(builder -> builder
                        .path("/movie/{id}")
                        .queryParam("api_key", apiKey)
                        .queryParam("language", "ko-KR")
                        .queryParam("include_image_language", "ko,null")
                        .queryParam("append_to_response", "videos,images,watch/providers,release_dates,credits")
                        .build(tmdbId))
                .retrieve()
                .onStatus(status -> status == HttpStatus.NOT_FOUND,
                        resp -> Mono.error(new BadRequestException(ErrorStatus.NOT_REGISTER_MOVIE_EXCEPTION.getMessage())))
                .bodyToMono(JsonNode.class)
                .block();

        if (root == null) {
            throw new InternalServerException(ErrorStatus.NO_RESPONSE_TMDB_EXCEPTION.getMessage());
        }

        // 개봉일 추출
        String releaseDateText = root.path("release_date").asText(null);

        LocalDate relDate = parseDate(releaseDateText);
        if (relDate == null) {
            throw new BadRequestException(ErrorStatus.NOT_RELEASE_MOVIE_EXCEPTION.getMessage());
        }

        int productionYear = relDate.getYear();

        // 한국 연령 등급 추출
        String ageCert = "";
        for (JsonNode node : root.path("release_dates").path("results")) {
            if ("KR".equals(node.path("iso_3166_1").asText())) {
                ageCert = node.path("release_dates").get(0).path("certification").asText();
                break;
            }
        }

        // 제작국가 추출 (첫번째)
        String prodCountry = "";
        JsonNode pcArr = root.path("production_countries");
        if (pcArr.isArray() && pcArr.size() > 0) {
            prodCountry = pcArr.get(0).path("name").asText();
        }

        // 영화 엔티티 필드
        Movie movie = Movie.builder()
                .tmdbId(root.get("id").asLong())
                .title(root.get("title").asText())
                .originalTitle(root.get("original_title").asText())
                .overview(root.get("overview").asText())
                .posterImg(root.path("poster_path").isNull()
                        ? null
                        : imageBaseUrl + root.path("poster_path").asText())
                .backgroundImg(root.path("backdrop_path").isNull()
                        ? null
                        : imageBaseUrl + root.path("backdrop_path").asText())
                .popcorn(0.0)
                .voteAverage(0.0)
                .releaseDate(relDate)
                .productionYear(productionYear)
                .productionCountry(prodCountry)
                .ageRating(ageCert)
                .runtime(root.get("runtime").asInt())
                .build();

        // 장르 조회 및 저장
        List<Long> genreIds = new ArrayList<>();
        root.path("genres").forEach(g -> genreIds.add(g.get("id").asLong()));
        Map<Long, Genre> existingGenres = genreRepository.findByTmdbIdIn(genreIds)
                .stream().collect(Collectors.toMap(Genre::getTmdbId, g -> g));
        for (JsonNode g : root.path("genres")) {
            long gid = g.get("id").asLong();
            Genre genre = existingGenres.computeIfAbsent(gid, id ->
                    genreRepository.save(Genre.builder()
                            .tmdbId(id)
                            .genreName(g.get("name").asText())
                            .build()));
            movie.getMovieGenres().add(
                    MovieGenre.builder().movie(movie).genre(genre).build()
            );
        }

        // 이미지 저장
        root.path("images").path("posters")
                .forEach(img -> addImage(movie, img.path("file_path").asText(null)));
        root.path("images").path("backdrops")
                .forEach(img -> addImage(movie, img.path("file_path").asText(null)));

        // 비디오 저장
        root.path("videos").path("results").forEach(v -> {
            if ("YouTube".equals(v.path("site").asText())) {
                movie.getMedia().add(
                        MovieImageVideo.builder()
                                .url("https://www.youtube.com/watch?v=" + v.path("key").asText())
                                .movieMediaType(MovieMediaType.VIDEO)
                                .movie(movie)
                                .build()
                );
            }
        });

        // 제공사 조회 및 저장 (한국기준)
        JsonNode kr = root.path("watch/providers").path("results").path("KR");
        if (!kr.isMissingNode()) {
            List<Long> pidList = new ArrayList<>();
            kr.path("flatrate").forEach(p -> pidList.add(p.get("provider_id").asLong()));
            kr.path("rent").forEach(p -> pidList.add(p.get("provider_id").asLong()));
            kr.path("buy").forEach(p -> pidList.add(p.get("provider_id").asLong()));

            Map<Long, Provider> existingProvs = providerRepository.findByTmdbIdIn(pidList)
                    .stream().collect(Collectors.toMap(Provider::getTmdbId, p -> p));

            kr.path("flatrate").forEach(p -> addProvider(movie, p, ProviderType.FLATRATE, existingProvs));
            kr.path("rent").forEach(p -> addProvider(movie, p, ProviderType.RENT, existingProvs));
            kr.path("buy").forEach(p -> addProvider(movie, p, ProviderType.BUY, existingProvs));
        }

        return movieRepository.save(movie);
    }

    private void addImage(Movie movie, String path) {
        if (path != null) {
            movie.getMedia().add(
                    MovieImageVideo.builder()
                            .url(imageBaseUrl + path)
                            .movieMediaType(MovieMediaType.IMAGE)
                            .movie(movie)
                            .build()
            );
        }
    }

    private LocalDate parseDate(String text) {
        if (text == null || text.isBlank()) return null;
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private void addProvider(Movie movie, JsonNode p, ProviderType type, Map<Long, Provider> cache) {
        long pid = p.get("provider_id").asLong();
        Provider prov = cache.computeIfAbsent(pid, id ->
                providerRepository.save(
                        Provider.builder()
                                .tmdbId(id)
                                .providerName(p.get("provider_name").asText())
                                .build()
                )
        );
        movie.getProviders().add(
                MovieProvider.builder()
                        .movie(movie)
                        .provider(prov)
                        .providerType(type)
                        .build()
        );
    }

    // 배우 정보 호출
    private List<CastResponseDTO> fetchCasts(Long tmdbId) {
        JsonNode root = tmdbWebClient.get()
                .uri(builder -> builder
                        .path("/movie/{id}/credits")
                        .queryParam("api_key", apiKey)
                        .queryParam("language", "ko-KR")
                        .build(tmdbId))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        if (root == null || !root.has("cast")) {
            return Collections.emptyList();
        }

        return StreamSupport.stream(root.get("cast").spliterator(), false)
                .map(c -> CastResponseDTO.builder()
                        .id(c.get("id").asLong())
                        .name(c.get("name").asText())
                        .profileImg(c.path("profile_path").isNull()
                                ? null
                                : imageBaseUrl + c.get("profile_path").asText())
                        .build())
                .collect(Collectors.toList());
    }
}
