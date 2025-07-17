package com.flipflick.backend.api.playlist.repository;

import com.flipflick.backend.api.playlist.entity.PlayList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayListRepository extends JpaRepository<PlayList, Long> {

    // 전체 플레이리스트 조회 (페이지네이션)
    @Query("SELECT p FROM PlayList p " +
            "WHERE p.hidden = false AND p.isDeleted = false " +
            "ORDER BY " +
            "CASE WHEN :sortBy = 'latest' THEN p.createdAt END DESC, " +
            "CASE WHEN :sortBy = 'oldest' THEN p.createdAt END ASC, " +
            "CASE WHEN :sortBy = 'popularity' THEN (SELECT COUNT(pb) FROM PlayListBookmark pb WHERE pb.playList = p) END DESC, " +
            "p.createdAt DESC")
    Page<PlayList> findAllByHiddenFalseAndIsDeletedFalse(@Param("sortBy") String sortBy, Pageable pageable);

    // 사용자가 만든 플레이리스트 조회 (페이지네이션)
    @Query("SELECT p FROM PlayList p " +
            "WHERE p.member.id = :userId AND p.isDeleted = false " +
            "ORDER BY p.createdAt DESC")
    Page<PlayList> findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);

    // 사용자가 북마크한 플레이리스트 조회 (페이지네이션) - hidden=false AND isDeleted=false 조건 모두 포함
    @Query("SELECT p FROM PlayList p " +
            "JOIN PlayListBookmark pb ON p.id = pb.playList.id " +
            "WHERE pb.member.id = :userId AND p.isDeleted = false AND p.hidden = false " +
            "ORDER BY pb.createdAt DESC")
    Page<PlayList> findBookmarkedByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);

    // 제목으로 플레이리스트 검색 (페이지네이션)
    @Query("SELECT p FROM PlayList p " +
            "WHERE p.hidden = false " +
            "AND p.title LIKE %:keyword% " +
            "ORDER BY p.createdAt DESC " )
    Page<PlayList> searchByTitleContaining(@Param("keyword") String keyword, Pageable pageable);

    // 소프트 삭제된 것 제외하고 단일 조회
    @Query("SELECT p FROM PlayList p WHERE p.id = :id AND p.isDeleted = false")
    Optional<PlayList> findByIdAndIsDeletedFalse(@Param("id") Long id);
}
