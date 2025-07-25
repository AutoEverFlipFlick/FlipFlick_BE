package com.flipflick.backend.api.playlist.repository;

import com.flipflick.backend.api.playlist.entity.PlayListBookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayListBookmarkRepository extends JpaRepository<PlayListBookmark, Long> {

    // 특정 사용자가 특정 플레이리스트를 북마크했는지 조회
    @Query("SELECT pb FROM PlayListBookmark pb WHERE pb.playList.id = :playListId AND pb.member.id = :memberId")
    Optional<PlayListBookmark> findByPlayListIdAndMemberId(@Param("playListId") Long playListId, @Param("memberId") Long memberId);
    
    // 특정 플레이리스트의 북마크 수 조회
    @Query("SELECT COUNT(pb) FROM PlayListBookmark pb WHERE pb.playList.id = :playListId")
    Integer countByPlayListId(@Param("playListId") Long playListId);

    // 사용자의 모든 북마크된 플레이리스트 ID 목록 조회 - hidden=false AND isDeleted=false 조건 모두 포함
    @Query("SELECT pb.playList.id FROM PlayListBookmark pb " +
            "WHERE pb.member.id = :userId AND pb.playList.isDeleted = false AND pb.playList.hidden = false " +
            "ORDER BY pb.createdAt DESC")
    List<Long> findPlayListIdsByMemberId(@Param("userId") Long userId);
}