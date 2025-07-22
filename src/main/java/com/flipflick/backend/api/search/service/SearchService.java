package com.flipflick.backend.api.search.service;

import com.flipflick.backend.api.follow.repository.FollowRepository;
import com.flipflick.backend.api.member.entity.Member;
import com.flipflick.backend.api.member.repository.MemberRepository;
import com.flipflick.backend.api.playlist.entity.MoviePlaylist;
import com.flipflick.backend.api.playlist.entity.PlayList;
import com.flipflick.backend.api.playlist.repository.MoviePlaylistRepository;
import com.flipflick.backend.api.playlist.repository.PlayListBookmarkRepository;
import com.flipflick.backend.api.playlist.repository.PlayListRepository;
import com.flipflick.backend.api.search.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class SearchService {

    @Value("${tmdb.api.key}")
    private String apiKey;

    @Value("${tmdb.api.image-base-url}")
    private String imageBaseUrl;

    private final WebClient tmdbWebClient;
    private final PlayListRepository playListRepository;
    private final MoviePlaylistRepository moviePlaylistRepository;
    private final PlayListBookmarkRepository playListBookmarkRepository;
    private final MemberRepository memberRepository;
    private final FollowRepository followRepository;

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

    // 플레이리스트 조회
    public PlayListPageResponseDTO searchPlaylist(SearchRequestDTO searchRequestDTO) {

        Pageable pageable = PageRequest.of(searchRequestDTO.getPage(), 20);

        // hidden=false, isDeleted=false, 제목 LIKE 검색
        Page<PlayList> page = playListRepository.searchByTitleContaining(
                searchRequestDTO.getQuery(), pageable
        );

        List<PlayListResponseDTO> content = page.stream()
                .map(playList -> {
                    Long id = playList.getId();

                    // 영화 개수, 북마크 개수, 썸네일 URL 조회
                    Integer movieCount = moviePlaylistRepository.countByPlayListId(id);
                    Integer bookmarkCount = playListBookmarkRepository.countByPlayListId(id);
                    String thumbnailUrl = moviePlaylistRepository
                            .findFirstByPlayListIdOrderByCreatedAtAsc(id)
                            .map(MoviePlaylist::getPosterUrl)
                            .orElse(null);

                    return PlayListResponseDTO.builder()
                            .playListId(id)
                            .title(playList.getTitle())
                            .nickname(playList.getMember().getNickname())
                            .thumbnailUrl(thumbnailUrl)
                            .movieCount(movieCount)
                            .bookmarkCount(bookmarkCount)
                            .build();
                })
                .collect(Collectors.toList());

        return PlayListPageResponseDTO.builder()
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .page(page.getNumber())
                .size(page.getSize())
                .isLast(page.isLast())
                .content(content)
                .build();
    }

    // 회원 검색 메서드
    public MemberListPageResponseDTO searchMember(SearchRequestDTO searchRequestDTO, Long currentUserId) {

        Pageable pageable = PageRequest.of(searchRequestDTO.getPage(), 20, Sort.by("nickname").ascending());
        Page<Member> page = memberRepository.findByNicknameContainingAndIsDeletedFalse(searchRequestDTO.getQuery(), pageable);

        // 검색된 회원 ID 목록
        List<Long> memberIds = page.stream()
                .map(Member::getId)
                .collect(Collectors.toList());

        // 팔로워 수 일괄 조회
        Map<Long, Long> followerCountMap = followRepository
                .countFollowersByFollowedIds(memberIds)
                .stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));

        // 로그인 유저가 팔로우 중인 ID 목록 일괄 조회
        final Set<Long> followedSet;
        if (currentUserId != null) {
            List<Long> followedIds = followRepository
                    .findFollowedIdsByFollowingAndFollowedIds(currentUserId, memberIds);
            followedSet = new HashSet<>(followedIds);
        } else {
            followedSet = Collections.emptySet();
        }

        List<MemberListResponseDTO> content = page.stream()
                .map(m -> MemberListResponseDTO.builder()
                        .memberId(m.getId())
                        .nickname(m.getNickname())
                        .followCnt(followerCountMap.getOrDefault(m.getId(), 0L))
                        .followed(followedSet.contains(m.getId()))
                        .profileImage(m.getProfileImage())
                        .build()
                )
                .collect(Collectors.toList());

        return MemberListPageResponseDTO.builder()
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .page(page.getNumber())
                .size(page.getSize())
                .isLast(page.isLast())
                .content(content)
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
