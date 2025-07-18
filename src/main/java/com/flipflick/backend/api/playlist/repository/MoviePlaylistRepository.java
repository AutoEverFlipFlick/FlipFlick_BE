package com.flipflick.backend.api.playlist.repository;

import com.flipflick.backend.api.playlist.entity.MoviePlaylist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface MoviePlaylistRepository extends JpaRepository<MoviePlaylist, Long> {

    // 특정 플레이리스트의 영화 목록 조회
    @Query("SELECT mp FROM MoviePlaylist mp " +
            "WHERE mp.playList.id = :playListId " +
            "ORDER BY mp.createdAt ASC")
    List<MoviePlaylist> findByPlayListIdOrderByCreatedAtAsc(@Param("playListId") Long playListId);

    // 특정 플레이리스트의 영화 수 조회
    @Query("SELECT COUNT(mp) FROM MoviePlaylist mp WHERE mp.playList.id = :playListId")
    Integer countByPlayListId(@Param("playListId") Long playListId);

    // 특정 플레이리스트의 첫 번째 영화 조회 (썸네일용)
    Optional<MoviePlaylist> findFirstByPlayListIdOrderByCreatedAtAsc(@Param("playListId") Long playListId);

    // 페이지네이션 메서드 추가
    @Query("SELECT mp FROM MoviePlaylist mp WHERE mp.playList.id = :playListId ORDER BY mp.createdAt ASC")
    Page<MoviePlaylist> findByPlayListIdOrderByCreatedAtAsc(@Param("playListId") Long playListId, Pageable pageable);

    // 중복 체크 - 같은 플레이리스트에 같은 영화가 있는지 확인
    boolean existsByPlayListIdAndTmdbId(Long playListId, Integer tmdbId);

    // 특정 플레이리스트에서 특정 TMDB ID들 삭제
    @Modifying
    @Query("DELETE FROM MoviePlaylist mp WHERE mp.playList.id = :playListId AND mp.tmdbId IN :tmdbIds")
    void deleteByPlayListIdAndTmdbIdIn(@Param("playListId") Long playListId, @Param("tmdbIds") Set<Integer> tmdbIds);
}