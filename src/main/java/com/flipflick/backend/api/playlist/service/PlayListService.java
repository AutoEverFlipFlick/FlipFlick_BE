package com.flipflick.backend.api.playlist.service;

import com.flipflick.backend.api.alarm.service.AlarmService;
import com.flipflick.backend.api.playlist.dto.PlayListRequestDto;
import com.flipflick.backend.api.playlist.dto.PlayListResponseDto;
import com.flipflick.backend.api.playlist.entity.MoviePlaylist;
import com.flipflick.backend.api.playlist.entity.PlayList;
import com.flipflick.backend.api.playlist.entity.PlayListBookmark;
import com.flipflick.backend.api.playlist.repository.MoviePlaylistRepository;
import com.flipflick.backend.api.playlist.repository.PlayListBookmarkRepository;
import com.flipflick.backend.api.playlist.repository.PlayListRepository;
import com.flipflick.backend.api.member.entity.Member;
import com.flipflick.backend.api.member.repository.MemberRepository;
import com.flipflick.backend.common.exception.BadRequestException;
import com.flipflick.backend.common.exception.NotFoundException;
import com.flipflick.backend.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlayListService {

    private final PlayListRepository playListRepository;
    private final PlayListBookmarkRepository playListBookmarkRepository;
    private final MoviePlaylistRepository moviePlaylistRepository;
    private final MemberRepository memberRepository;
    private final AlarmService alarmService;

    // 1. 전체 플레이리스트 조회
    @Transactional(readOnly = true)
    public PlayListResponseDto.PlaylistPageResponse getAllPlayLists(String sortBy, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PlayList> playListPage = playListRepository.findAllByHiddenFalseAndIsDeletedFalse(sortBy, pageable);

        Page<PlayListResponseDto.Summary> summaryPage = playListPage.map(this::convertToSummary);
        return PlayListResponseDto.PlaylistPageResponse.from(summaryPage);
    }

    // 2. 내가 찜한 플레이리스트 조회
    @Transactional(readOnly = true)
    public PlayListResponseDto.PlaylistPageResponse getBookmarkedPlayLists(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PlayList> playListPage = playListRepository.findBookmarkedByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(userId, pageable);

        Page<PlayListResponseDto.Summary> summaryPage = playListPage.map(this::convertToSummary);
        return PlayListResponseDto.PlaylistPageResponse.from(summaryPage);
    }

    // 3. 내가 작성한 플레이리스트 조회
    @Transactional(readOnly = true)
    public PlayListResponseDto.PlaylistPageResponse getMyPlayLists(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PlayList> playListPage = playListRepository.findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(userId, pageable);

        Page<PlayListResponseDto.Summary> summaryPage = playListPage.map(this::convertToSummary);
        return PlayListResponseDto.PlaylistPageResponse.from(summaryPage);
    }

    // 4. 플레이리스트 상세 조회
    @Transactional(readOnly = true)
    public PlayListResponseDto.PlaylistDetail getPlayListDetail(Long playListId, int page, int size) {
        PlayList playList = playListRepository.findByIdAndIsDeletedFalse(playListId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.PLAYLIST_NOT_FOUND.getMessage()));

        // 페이지네이션으로 영화 목록 조회
        Pageable pageable = PageRequest.of(page, size);
        Page<MoviePlaylist> moviePlaylistPage = moviePlaylistRepository.findByPlayListIdOrderByCreatedAtAsc(playListId, pageable);

        // 전체 영화 개수 (페이지네이션과 별개)
        Integer totalMovieCount = moviePlaylistRepository.countByPlayListId(playListId);
        Integer bookmarkCount = playListBookmarkRepository.countByPlayListId(playListId);

        // MoviePlaylist → MovieInfo 변환
        Page<PlayListResponseDto.MovieInfo> movieInfoPage = moviePlaylistPage.map(mp ->
                PlayListResponseDto.MovieInfo.builder()
                        .movieId(Long.valueOf(mp.getTmdbId()))
                        .title(mp.getTitle())
                        .posterUrl(mp.getPosterUrl())
                        .releaseDate(mp.getReleaseDate())
                        .build()
        );

        // 페이지네이션 응답 생성
        PlayListResponseDto.MoviePageResponse moviePageResponse = PlayListResponseDto.MoviePageResponse.from(movieInfoPage);

        return PlayListResponseDto.PlaylistDetail.builder()
                .playListId(playList.getId())
                .title(playList.getTitle())
                .nickname(playList.getMember().getNickname())
                .hidden(playList.getHidden())
                .movieCount(totalMovieCount)  // 전체 영화 개수
                .bookmarkCount(bookmarkCount)
                .movies(moviePageResponse)  // 페이지네이션된 영화 목록
                .build();
    }

    // 5. 플레이리스트 생성
    @Transactional
    public PlayListResponseDto.PlaylistCreate createPlayList(Long userId, PlayListRequestDto.PlaylistCreate request) {
        Member member = memberRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOT_FOUND.getMessage()));

        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new BadRequestException(ErrorStatus.PLAYLIST_CREATION_FAILED.getMessage());
        }

        PlayList playList = PlayList.builder()
                .title(request.getTitle())
                .hidden(request.getHidden())
                .member(member)
                .build();

        playList = playListRepository.save(playList);

        if (request.getMovies() != null && !request.getMovies().isEmpty()) {
            for (PlayListRequestDto.MovieInfo movieInfo : request.getMovies()) {
                if (movieInfo.getTmdbId() != null && movieInfo.getTmdbId() > 0) {
                    MoviePlaylist moviePlaylist = MoviePlaylist.builder()
                            .playList(playList)
                            .title(movieInfo.getTitle())  // title 저장
                            .releaseDate(movieInfo.getReleaseDate())
                            .tmdbId(movieInfo.getTmdbId())
                            .posterUrl(movieInfo.getPosterUrl())
                            .build();

                    moviePlaylistRepository.save(moviePlaylist);
                }
            }
        }

        try {
            if (!playList.getHidden()) { // 공개 플레이리스트인 경우만
                alarmService.createPlaylistCreateAlarmForFollowers(userId, request.getTitle());
            }
        } catch (Exception e) {
            log.error("플레이리스트 생성 알림 전송 실패 - 사용자: {}, 제목: {}", member.getNickname(), request.getTitle(), e);
        }

        return PlayListResponseDto.PlaylistCreate.builder()
                .playListId(playList.getId())
                .title(playList.getTitle())
                .build();
    }

    // 6. 플레이리스트 북마크 토글
    @Transactional
    public boolean toggleBookmark(Long userId, Long playListId) {
        Member member = memberRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOT_FOUND.getMessage()));

        PlayList playList = playListRepository.findById(playListId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.PLAYLIST_NOT_FOUND.getMessage()));

        // 본인이 만든 플레이리스트는 북마크할 수 없음
        if (playList.getMember().getId().equals(userId)) {
            throw new BadRequestException(ErrorStatus.SELF_BOOKMARK_NOT_ALLOWED.getMessage());
        }

        PlayListBookmark existingBookmark = playListBookmarkRepository
                .findByPlayListIdAndMemberId(playListId, userId)
                .orElse(null);

        if (existingBookmark != null) {
            // 북마크 해제
            playListBookmarkRepository.delete(existingBookmark);
            return false;
        } else {
            // 북마크 추가
            PlayListBookmark bookmark = PlayListBookmark.builder()
                    .playList(playList)
                    .member(member)
                    .build();
            playListBookmarkRepository.save(bookmark);
            return true;
        }
    }

    // 7. 플레이리스트 수정
    @Transactional
    public PlayListResponseDto.PlaylistUpdate updatePlayList(Long userId, Long playListId, PlayListRequestDto.PlaylistUpdate request) {
        Member member = memberRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOT_FOUND.getMessage()));

        PlayList playList = playListRepository.findByIdAndIsDeletedFalse(playListId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.PLAYLIST_NOT_FOUND.getMessage()));

        if (!playList.getMember().getId().equals(userId)) {
            throw new BadRequestException(ErrorStatus.PLAYLIST_ACCESS_DENIED.getMessage());
        }

        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new BadRequestException(ErrorStatus.PLAYLIST_CREATION_FAILED.getMessage());
        }

        playList.updateInfo(request.getTitle(), request.getHidden());

        List<MoviePlaylist> existingMovies = moviePlaylistRepository.findByPlayListIdOrderByCreatedAtAsc(playListId);

        Set<Integer> existingTmdbIds = existingMovies.stream()
                .map(MoviePlaylist::getTmdbId)
                .collect(Collectors.toSet());

        if (request.getMovies() == null) {
            request.setMovies(new ArrayList<>());
        }

        List<PlayListRequestDto.MovieInfo> validMovies = request.getMovies().stream()
                .filter(movie -> movie.getTmdbId() != null && movie.getTmdbId() > 0)
                .collect(Collectors.toList());

        Set<Integer> newTmdbIds = validMovies.stream()
                .map(PlayListRequestDto.MovieInfo::getTmdbId)
                .collect(Collectors.toSet());

        Set<Integer> toDelete = new HashSet<>(existingTmdbIds);
        toDelete.removeAll(newTmdbIds);

        Set<Integer> toAdd = new HashSet<>(newTmdbIds);
        toAdd.removeAll(existingTmdbIds);

        if (!toDelete.isEmpty()) {
            moviePlaylistRepository.deleteByPlayListIdAndTmdbIdIn(playListId, toDelete);
        }

        int addedCount = 0;
        if (!toAdd.isEmpty()) {
            Map<Integer, PlayListRequestDto.MovieInfo> newMovieMap = validMovies.stream()
                    .filter(movie -> movie.getTmdbId() != null)
                    .collect(Collectors.toMap(
                            PlayListRequestDto.MovieInfo::getTmdbId,
                            movieInfo -> movieInfo,
                            (existing, replacement) -> existing
                    ));

            for (Integer tmdbId : toAdd) {
                PlayListRequestDto.MovieInfo movieInfo = newMovieMap.get(tmdbId);
                if (movieInfo != null) {
                    MoviePlaylist moviePlaylist = MoviePlaylist.builder()
                            .playList(playList)
                            .title(movieInfo.getTitle())  // title 저장
                            .releaseDate(movieInfo.getReleaseDate())
                            .tmdbId(tmdbId)
                            .posterUrl(movieInfo.getPosterUrl())
                            .build();

                    moviePlaylistRepository.save(moviePlaylist);
                    addedCount++;
                }
            }
        }

        Integer finalMovieCount = moviePlaylistRepository.countByPlayListId(playListId);

        return PlayListResponseDto.PlaylistUpdate.builder()
                .playListId(playListId)
                .title(playList.getTitle())
                .addedMovieCount(addedCount)
                .removedMovieCount(toDelete.size())
                .totalMovieCount(finalMovieCount)
                .build();
    }

    // 8. 플레이리스트 소프트 삭제
    @Transactional
    public PlayListResponseDto.PlaylistDelete deletePlayList(Long userId, Long playListId) {
        Member member = memberRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOT_FOUND.getMessage()));

        PlayList playList = playListRepository.findByIdAndIsDeletedFalse(playListId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.PLAYLIST_NOT_FOUND.getMessage()));

        if (!playList.getMember().getId().equals(member.getId())) {
            throw new BadRequestException(ErrorStatus.PLAYLIST_ACCESS_DENIED.getMessage());
        }

        playList.softDelete();

        return PlayListResponseDto.PlaylistDelete.builder()
                .title(playList.getTitle())
                .build();
    }

    //9. 플레이리스트 제목 검색 (페이지네이션)
    public PlayListResponseDto.PlaylistPageResponse searchPlayLists(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PlayList> playListPage = playListRepository.searchByTitleContaining(keyword, pageable);

        Page<PlayListResponseDto.Summary> summaryPage = playListPage.map(this::convertToSummary);
        return PlayListResponseDto.PlaylistPageResponse.from(summaryPage);
    }

    //10. 사용자 북마크 플레이리스트 조회
    @Transactional(readOnly = true)
    public PlayListResponseDto.BookmarkIds getBookmarkedPlayListIds(Long userId) {
        // 사용자 존재 확인
        Member member = memberRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOT_FOUND.getMessage()));

        // 북마크된 플레이리스트 ID 목록 조회
        List<Long> playListIds = playListBookmarkRepository.findPlayListIdsByMemberId(member.getId());

        return PlayListResponseDto.BookmarkIds.builder()
                .playListIds(playListIds)
                .totalCount(playListIds.size())
                .build();
    }

    //11. 닉네임으로 플레이리스트 조회 (최신순, 공개된 것만)
    @Transactional(readOnly = true)
    public PlayListResponseDto.PlaylistPageResponse getPlayListsByNickname(String nickname, int page, int size) {
        // 닉네임으로 사용자 존재 확인
        Member member = memberRepository.findByNicknameAndIsDeletedFalse(nickname)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOT_FOUND.getMessage()));

        Pageable pageable = PageRequest.of(page, size);
        Page<PlayList> playListPage = playListRepository.findByMemberNicknameAndIsDeletedFalseAndHiddenFalseOrderByCreatedAtDesc(nickname, pageable);

        Page<PlayListResponseDto.Summary> summaryPage = playListPage.map(playList -> {
            // 첫 번째 영화의 포스터를 썸네일로 사용
            String thumbnailUrl = moviePlaylistRepository.findFirstByPlayListIdOrderByCreatedAtAsc(playList.getId())
                    .map(MoviePlaylist::getPosterUrl)
                    .orElse(null);

            Integer movieCount = moviePlaylistRepository.countByPlayListId(playList.getId());
            Integer bookmarkCount = playListBookmarkRepository.countByPlayListId(playList.getId());

            return PlayListResponseDto.Summary.builder()
                    .playListId(playList.getId())
                    .title(playList.getTitle())
                    .nickname(playList.getMember().getNickname())
                    .thumbnailUrl(thumbnailUrl)
                    .movieCount(movieCount)
                    .bookmarkCount(bookmarkCount)
                    .build();
        });

        return PlayListResponseDto.PlaylistPageResponse.from(summaryPage);
    }

    // PlayList를 Summary DTO로 변환
    private PlayListResponseDto.Summary convertToSummary(PlayList playList) {
        Optional<MoviePlaylist> firstMovie = moviePlaylistRepository.findFirstByPlayListIdOrderByCreatedAtAsc(playList.getId());
        Integer movieCount = moviePlaylistRepository.countByPlayListId(playList.getId());
        Integer bookmarkCount = playListBookmarkRepository.countByPlayListId(playList.getId());

        return PlayListResponseDto.Summary.builder()
                .playListId(playList.getId())
                .title(playList.getTitle())
                .nickname(playList.getMember().getNickname())
                .thumbnailUrl(firstMovie.map(MoviePlaylist::getPosterUrl).orElse(null))
                .movieCount(movieCount)
                .bookmarkCount(bookmarkCount)
                .build();
    }
}