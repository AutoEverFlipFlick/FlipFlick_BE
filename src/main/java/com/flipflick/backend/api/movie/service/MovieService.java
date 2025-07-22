package com.flipflick.backend.api.movie.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.flipflick.backend.api.member.entity.Member;
import com.flipflick.backend.api.member.repository.MemberRepository;
import com.flipflick.backend.api.movie.dto.*;
import com.flipflick.backend.api.movie.entity.*;
import com.flipflick.backend.api.movie.repository.*;
import com.flipflick.backend.api.review.entity.LikeHateType;
import com.flipflick.backend.common.exception.BadRequestException;
import com.flipflick.backend.common.exception.InternalServerException;
import com.flipflick.backend.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

    @Value("${kobis.api.key}")
    private String kobisKey;

    private final WebClient tmdbWebClient;
    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final ProviderRepository providerRepository;
    private final BookmarkRepository bookmarkRepository;
    private final MemberRepository memberRepository;
    private final WatchedRepository watchedRepository;
    private final MovieLikeHateRepository movieLikeHateRepository;
    private final MoviePopcornScoreService moviePopcornScoreService;
    private final WebClient kobisClient;
    private final RedisTemplate<String,Object> redis;

    // 영화 상세 조회 메서드(DB에 영화데이터가 없으면 TMDB호출 및 저장후 반환)
    @Transactional
    public MovieDetailResponseDTO viewMovieDetail(SearchRequestIdDTO searchRedquestIdDTO, Long memberId) {
        Long tmdbId = searchRedquestIdDTO.getTmdbId();

        // 페치 조인으로 genre, media, provider 모두 미리 가져옴
        Movie movie = movieRepository.findWithAllByTmdbId(tmdbId)
                .orElseGet(() -> fetchAndSaveMovie(tmdbId));

        List<CastResponseDTO> casts = fetchCasts(tmdbId);

        boolean myBookmark  = false;
        boolean myWatched   = false;
        boolean myLike      = false;
        boolean myHate      = false;

        if (memberId != null) {
            Member member = memberRepository.findByIdAndIsDeletedFalse(memberId)
                    .orElseThrow(() -> new BadRequestException(ErrorStatus.INCORRECT_USER_EXCEPTION.getMessage()));

            myBookmark = bookmarkRepository.existsByMemberAndMovie(member, movie);
            myWatched = watchedRepository.existsByMemberAndMovie(member, movie);
            myLike = movieLikeHateRepository.existsByMemberAndMovieAndType(member, movie, LikeHateType.LIKE);
            myHate = movieLikeHateRepository.existsByMemberAndMovieAndType(member, movie, LikeHateType.HATE);
        }

        return MovieDetailResponseDTO.builder()
                .movieId(movie.getId())
                .tmdbId(movie.getTmdbId())
                .title(movie.getTitle())
                .originalTitle(movie.getOriginalTitle())
                .overview(movie.getOverview())
                .posterImg(movie.getPosterImg())
                .backgroundImg(movie.getBackgroundImg())
                .voteAverage(movie.getVoteAverage())
                .popcorn(movie.getPopcorn())
                .likeCnt(movie.getLikeCnt())
                .hateCnt(movie.getHateCnt())
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
                .myBookmark(myBookmark)
                .myWatched(myWatched)
                .myLike(myLike)
                .myHate(myHate)
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

    // 영화 찜 토글
    @Transactional
    public void movieBookmark(MovieBWLHRequestDTO movieBWLHRequestDTO, Long memberId) {

        // 영화 존재 확인
        Movie movie = movieRepository.findById(movieBWLHRequestDTO.getMovieId())
                .orElseThrow(() -> new BadRequestException(ErrorStatus.NOT_REGISTER_MOVIE_EXCEPTION.getMessage()));

        // 회원 존재 확인
        Member member = memberRepository.findByIdAndIsDeletedFalse(memberId)
                .orElseThrow(() -> new BadRequestException(ErrorStatus.INCORRECT_USER_EXCEPTION.getMessage()));

        // 찜 여부 체크 → 있으면 delete, 없으면 save
        bookmarkRepository.findByMemberAndMovie(member, movie)
                .ifPresentOrElse(
                        bm -> bookmarkRepository.delete(bm), () -> {
                            Bookmark newBm = Bookmark.builder()
                                    .movie(movie)
                                    .member(member)
                                    .build();
                            bookmarkRepository.save(newBm);
                        }
                );
    }

    // 내가 찜한 영화들 조회
    @Transactional(readOnly = true)
    public MovieBWLHListResponseDTO getBookmarkedMovies(Long memberId, int page, int size) {

        // 회원 검증
        if (!memberRepository.existsByIdAndIsDeletedFalse(memberId)) {
            throw new BadRequestException(ErrorStatus.INCORRECT_USER_EXCEPTION.getMessage());
        }

        // 내림차순 정렬
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Bookmark> bmPage = bookmarkRepository.findByMember_Id(memberId, pageRequest);

        var content = bmPage.getContent().stream()
                .map(bm -> {
                    var m = bm.getMovie();
                    return new MovieBWLHResponseDTO(
                            m.getTmdbId(),
                            m.getPosterImg(),
                            m.getTitle(),
                            m.getReleaseDate().getYear()
                    );
                })
                .collect(Collectors.toList());

        return MovieBWLHListResponseDTO.builder()
                .totalElements(bmPage.getTotalElements())
                .totalPages(bmPage.getTotalPages())
                .page(bmPage.getNumber())
                .size(bmPage.getSize())
                .isLast(bmPage.isLast())
                .content(content)
                .build();
    }

    // 영화 봤어요 토글
    @Transactional
    public void movieWatched(MovieBWLHRequestDTO movieBWLHRequestDTO, Long memberId) {

        // 영화 존재 확인
        Movie movie = movieRepository.findById(movieBWLHRequestDTO.getMovieId())
                .orElseThrow(() -> new BadRequestException(ErrorStatus.NOT_REGISTER_MOVIE_EXCEPTION.getMessage()));

        // 회원 존재 확인
        Member member = memberRepository.findByIdAndIsDeletedFalse(memberId)
                .orElseThrow(() -> new BadRequestException(ErrorStatus.INCORRECT_USER_EXCEPTION.getMessage()));

        // 봤어요 여부 체크 → 있으면 delete, 없으면 save
        watchedRepository.findByMemberAndMovie(member, movie)
                .ifPresentOrElse(
                        watched -> watchedRepository.delete(watched), () -> {
                            Watched watched = Watched.builder()
                                    .movie(movie)
                                    .member(member)
                                    .build();
                            watchedRepository.save(watched);
                        }
                );
    }

    // 내가 찜한 영화들 조회
    @Transactional(readOnly = true)
    public MovieBWLHListResponseDTO getMovieWatched(Long memberId, int page, int size) {

        // 회원 검증
        if (!memberRepository.existsByIdAndIsDeletedFalse(memberId)) {
            throw new BadRequestException(ErrorStatus.INCORRECT_USER_EXCEPTION.getMessage());
        }

        // 내림차순 정렬
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Watched> watchedPage = watchedRepository.findByMember_Id(memberId, pageRequest);

        var content = watchedPage.getContent().stream()
                .map(watched -> {
                    var w = watched.getMovie();
                    return new MovieBWLHResponseDTO(
                            w.getTmdbId(),
                            w.getPosterImg(),
                            w.getTitle(),
                            w.getReleaseDate().getYear()
                    );
                })
                .collect(Collectors.toList());

        return MovieBWLHListResponseDTO.builder()
                .totalElements(watchedPage.getTotalElements())
                .totalPages(watchedPage.getTotalPages())
                .page(watchedPage.getNumber())
                .size(watchedPage.getSize())
                .isLast(watchedPage.isLast())
                .content(content)
                .build();
    }

    // 좋아요 싫어요 토글
    @Transactional
    public void movieLikeHate(Long memberId, MovieLikeHateRequestDTO movieLikeHateRequestDTO) {

        // 영화와 회원 조회
        Movie movie = movieRepository.findById(movieLikeHateRequestDTO.getMovieId())
                .orElseThrow(() -> new BadRequestException(ErrorStatus.NOT_REGISTER_MOVIE_EXCEPTION.getMessage()));
        Member member = memberRepository.findByIdAndIsDeletedFalse(memberId)
                .orElseThrow(() -> new BadRequestException(ErrorStatus.INCORRECT_USER_EXCEPTION.getMessage()));

        LikeHateType requested = movieLikeHateRequestDTO.getLikeHateType();

        // 기존 토글 상태 조회
        Optional<MovieLikeHate> opt = movieLikeHateRepository.findByMemberAndMovie(member, movie);

        if (opt.isPresent()) {
            MovieLikeHate existing = opt.get();

            if (existing.getType() == requested) {

                // 같은 버튼 연타
                movieLikeHateRepository.delete(existing);
                if (requested == LikeHateType.LIKE)   movie.decrementLike();
                else                                    movie.decrementHate();
            } else {
                // LIKE → HATE 또는 HATE → LIKE 전환
                // 기존 레코드 삭제 + 카운트 감소
                movieLikeHateRepository.delete(existing);
                if (existing.getType() == LikeHateType.LIKE) {
                    movie.decrementLike();
                } else {
                    movie.decrementHate();
                }

                // 새 레코드 생성 + 카운트 증가
                MovieLikeHate fresh = MovieLikeHate.builder()
                        .movie(movie)
                        .member(member)
                        .type(requested)
                        .build();
                movieLikeHateRepository.save(fresh);
                if (requested == LikeHateType.LIKE)   movie.incrementLike();
                else                                    movie.incrementHate();
            }
        } else {
            // 처음 누르는 경우
            MovieLikeHate fresh = MovieLikeHate.builder()
                    .movie(movie)
                    .member(member)
                    .type(requested)
                    .build();
            movieLikeHateRepository.save(fresh);
            if (requested == LikeHateType.LIKE)   movie.incrementLike();
            else                                    movie.incrementHate();
        }
    }

    // 좋아요 누른 영화 리스트 조회
    @Transactional(readOnly = true)
    public MovieBWLHListResponseDTO getMovieLike(Long memberId, int page, int size) {

        if (!memberRepository.existsByIdAndIsDeletedFalse(memberId)) {
            throw new BadRequestException(ErrorStatus.INCORRECT_USER_EXCEPTION.getMessage());
        }

        // 최신 좋아요 순
        PageRequest pr = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<MovieLikeHate> pageLH = movieLikeHateRepository
                .findByMember_IdAndType(memberId, LikeHateType.LIKE, pr);

        var content = pageLH.getContent().stream()
                .map(mh -> {
                    Movie m = mh.getMovie();
                    return new MovieBWLHResponseDTO(
                            m.getTmdbId(),
                            m.getPosterImg(),
                            m.getTitle(),
                            m.getReleaseDate().getYear()
                    );
                })
                .collect(Collectors.toList());

        return MovieBWLHListResponseDTO.builder()
                .totalElements(pageLH.getTotalElements())
                .totalPages(pageLH.getTotalPages())
                .page(pageLH.getNumber())
                .size(pageLH.getSize())
                .isLast(pageLH.isLast())
                .content(content)
                .build();
    }

    /**
     * Popcorn 점수 기준 TOP 영화 조회
     */
    @Transactional(readOnly = true)
    public List<MovieBWLHResponseDTO> getTopMoviesByPopcornScore(int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit);
        Page<Movie> topMovies = movieRepository.findTopMoviesByPopcornScore(pageRequest);

        return topMovies.getContent().stream()
                .map(movie -> new MovieBWLHResponseDTO(
                        movie.getTmdbId(),
                        movie.getPosterImg(),
                        movie.getTitle(),
                        movie.getReleaseDate().getYear()
                ))
                .collect(Collectors.toList());
    }

    /**
     * 수동으로 Popcorn 점수 재계산 (관리자용)
     */
    @Transactional
    public void manualRecalculatePopcornScores() {
        moviePopcornScoreService.recalculateAllPopcornScores();
    }

    // 박스오피스 TOP10 조회
    public BoxOfficeResponseDTO getYesterdayBoxOffice(String today) {
        String cacheKey = "boxoffice:" + today;

        // 캐시 체크
        var cached = redis.opsForValue().get(cacheKey);
        if (cached != null) {
            return (BoxOfficeResponseDTO) cached;
        }

        // 어제 날짜 계산
        LocalDate date = LocalDate.parse(today);
        String targetDt = date.minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // KOBIS 호출
        String kobisUrl = "/kobisopenapi/webservice/rest/boxoffice/searchDailyBoxOfficeList.json";
        JsonNode kobisRoot = kobisClient.get()
                .uri(uri -> uri
                        .path(kobisUrl)
                        .queryParam("key", kobisKey)
                        .queryParam("targetDt", targetDt)
                        .build())
                .retrieve().bodyToMono(JsonNode.class).block();

        List<JsonNode> list = kobisRoot
                .path("boxOfficeResult")
                .path("dailyBoxOfficeList")
                .elements()
                .hasNext()
                ? StreamSupport.stream(
                        kobisRoot
                                .path("boxOfficeResult")
                                .path("dailyBoxOfficeList")
                                .spliterator(), false)
                .limit(10)
                .collect(Collectors.toList())
                : List.of();

        // TMDb 매핑
        List<BoxOfficeMovieDTO> result = new ArrayList<>();
        for (JsonNode item : list) {
            String name = item.get("movieNm").asText();
            int rank  = item.get("rank").asInt();
            int year  = Integer.parseInt(item.get("openDt").asText().substring(0,4));

            JsonNode search = tmdbWebClient.get()
                    .uri(uri -> uri
                            .path("/search/movie")
                            .queryParam("api_key", apiKey)
                            .queryParam("language", "ko-KR")
                            .queryParam("query", name)
                            .queryParam("year", year)
                            .build())
                    .retrieve().bodyToMono(JsonNode.class).block();

            JsonNode first = search.path("results").isArray() && search.path("results").size()>0
                    ? search.path("results").get(0)
                    : null;
            if (first == null) continue;

            Long tmdbId = first.get("id").asLong();
            String posterPath = first.path("poster_path").isNull()
                    ? null
                    : imageBaseUrl + first.get("poster_path").asText();

            result.add(new BoxOfficeMovieDTO(tmdbId, posterPath, name, rank));
        }

        BoxOfficeResponseDTO boxOfficeResponseDTO = new BoxOfficeResponseDTO(result);
        // Redis에 캐싱 (TTL: 24시간)
        redis.opsForValue().set(cacheKey, boxOfficeResponseDTO, Duration.ofHours(24));

        return boxOfficeResponseDTO;
    }

}
