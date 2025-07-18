package com.flipflick.backend.api.cast.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.flipflick.backend.api.cast.dto.CastDetailResponseDTO;
import com.flipflick.backend.api.cast.dto.FilmographyDTO;
import com.flipflick.backend.api.cast.entity.Cast;
import com.flipflick.backend.api.cast.entity.Filmography;
import com.flipflick.backend.api.cast.entity.Gender;
import com.flipflick.backend.api.cast.repository.CastRepository;
import com.flipflick.backend.api.cast.dto.SearchRequestIdDTO;
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

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CastService {

    @Value("${tmdb.api.key}")
    private String apiKey;

    @Value("${tmdb.api.image-base-url}")
    private String imageBaseUrl;

    private final WebClient tmdbWebClient;
    private final CastRepository castRepository;

    // 배우 상세 조회 메서드
    @Transactional
    public CastDetailResponseDTO viewCastDetail(SearchRequestIdDTO dto) {
        Long tmdbId = dto.getTmdbId();

        // DB에 있으면 불러오기, 없으면 TMDB에서 조회후 저장
        Cast cast = castRepository.findWithFilmographiesByTmdbId(tmdbId)
                .orElseGet(() -> fetchAndSaveCast(tmdbId));

        return CastDetailResponseDTO.builder()
                .tmdbId(cast.getTmdbId())
                .name(cast.getName())
                .gender(cast.getGender())
                .profileImage(cast.getProfileImage())
                .placeOfBirth(cast.getPlaceOfBirth())
                .birthday(cast.getBirthday())
                .deathday(cast.getDeathday())
                .filmographies(
                        cast.getFilmographies().stream()
                                .sorted(Comparator.comparing(
                                                Filmography::getReleaseDate,
                                                Comparator.nullsLast(Comparator.naturalOrder()))
                                )
                                .map(f -> FilmographyDTO.builder()
                                        .tmdbId(f.getTmdbId())
                                        .posterImage(f.getPosterImage())
                                        .name(f.getName())
                                        .releaseDate(f.getReleaseDate())
                                        .build())
                                .collect(Collectors.toList())
                )
                .build();
    }

    private Cast fetchAndSaveCast(Long tmdbId) {
        URI uri = URI.create("/person/" + tmdbId
                + "?api_key=" + apiKey
                + "&language=ko-KR"
                + "&append_to_response=combined_credits,images,external_ids");

        JsonNode root = tmdbWebClient.get()
                .uri(builder -> builder
                        .path(uri.getPath())
                        .query(uri.getQuery())
                        .build())
                .retrieve()
                .onStatus(status -> status == HttpStatus.NOT_FOUND,
                        resp -> Mono.error(new BadRequestException(ErrorStatus.NOT_REGISTER_CAST_EXCEPTION.getMessage())))
                .bodyToMono(JsonNode.class)
                .block();

        if (root == null) {
            throw new InternalServerException(ErrorStatus.NO_RESPONSE_TMDB_EXCEPTION.getMessage());
        }

        // 기본 배우 정보
        String profilePath = root.path("profile_path").asText(null);
        Cast cast = Cast.builder()
                .tmdbId(root.path("id").asLong())
                .name(root.path("name").asText())
                .gender(parseGender(root.path("gender").asInt()))
                .profileImage(profilePath != null ? imageBaseUrl + profilePath : null)
                .placeOfBirth(root.path("place_of_birth").asText(null))
                .birthday(parseDate(root.path("birthday").asText(null)))
                .deathday(parseDate(root.path("deathday").asText(null)))
                .build();

        // combined_credits.cast 순회 → media_type="movie" 인 경우만 필모그래피로 추가
        root.path("combined_credits").path("cast").forEach(cn -> {
            if (!"movie".equalsIgnoreCase(cn.path("media_type").asText())) {
                return; // TV 제외
            }
            Long workId = cn.path("id").asLong();
            String posterPath = cn.path("poster_path").asText(null);
            String title      = cn.path("title").asText(null);
            String dateStr    = cn.path("release_date").asText(null);

            Filmography fg = Filmography.builder()
                    .tmdbId(workId)
                    .posterImage(posterPath != null ? imageBaseUrl + posterPath : null)
                    .name(title)
                    .releaseDate(parseDate(dateStr))
                    .cast(cast)
                    .build();

            cast.getFilmographies().add(fg);
        });

        return castRepository.save(cast);
    }

    private LocalDate parseDate(String text) {
        if (text == null || text.isBlank()) return null;
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private Gender parseGender(int code) {
        switch (code) {
            case 2: return Gender.MALE;
            case 1: return Gender.FEMALE;
            default: return null;
        }
    }
}
