package com.flipflick.backend.api.follow.repository;

import com.flipflick.backend.api.follow.entity.Follow;
import com.flipflick.backend.api.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    // 팔로우 여부 확인
    @Query("""
        SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END
        FROM Follow f
        WHERE f.following = :following
          AND f.followed = :followed
          AND f.following.isDeleted = false
          AND f.followed.isDeleted = false
    """)
    boolean existsByFollowingAndFollowedAndBothNotDeleted(
            @Param("following") Member following,
            @Param("followed") Member followed
    );

    // 단건 조회
    @Query("""
        SELECT f FROM Follow f
        WHERE f.following = :following
          AND f.followed = :followed
          AND f.following.isDeleted = false
          AND f.followed.isDeleted = false
    """)
    Optional<Follow> findByFollowingAndFollowedAndBothNotDeleted(
            @Param("following") Member following,
            @Param("followed") Member followed
    );

    // 팔로워 수 (삭제된 팔로워 제외)
    @Query("""
        SELECT COUNT(f) FROM Follow f
        WHERE f.followed = :followed
          AND f.following.isDeleted = false
    """)
    long countByFollowedAndFollowingIsDeletedFalse(@Param("followed") Member followed);

    // 팔로잉 수 (삭제된 대상 제외)
    @Query("""
        SELECT COUNT(f) FROM Follow f
        WHERE f.following = :following
          AND f.followed.isDeleted = false
    """)
    long countByFollowingAndFollowedIsDeletedFalse(@Param("following") Member following);

    // 팔로워 목록 (삭제되지 않은 사용자만)
    @Query("""
        SELECT f FROM Follow f
        WHERE f.followed = :followed
          AND f.following.isDeleted = false
    """)
    Page<Follow> findAllByFollowedAndFollowingIsDeletedFalse(@Param("followed") Member followed, Pageable pageable);

    // 팔로잉 목록 (삭제되지 않은 사용자만)
    @Query("""
        SELECT f FROM Follow f
        WHERE f.following = :following
          AND f.followed.isDeleted = false
    """)
    Page<Follow> findAllByFollowingAndFollowedIsDeletedFalse(@Param("following") Member following, Pageable pageable);

    // 단순 전체 조회 (삭제 필터 없음) - 필요 시 유지
    List<Follow> findAllByFollowed(Member followed);

    // 특정 날짜 기준 팔로워 수
    @Query("""
        SELECT COUNT(f) FROM Follow f
        WHERE f.followed.id = :memberId
          AND f.createdAt < :endOfDay
          AND f.following.isDeleted = false
    """)
    Integer countFollowsByMemberIdUntilDate(
            @Param("memberId") Long memberId,
            @Param("endOfDay") LocalDateTime endOfDay
    );

    // 여러 명에 대해 팔로워 수 조회 (삭제되지 않은 회원만 대상)
    @Query("""
        SELECT f.followed.id, COUNT(f)
        FROM Follow f
        WHERE f.followed.id IN :ids
          AND f.following.isDeleted = false
        GROUP BY f.followed.id
    """)
    List<Object[]> countFollowersByFollowedIds(@Param("ids") List<Long> ids);

    // 로그인 유저가 팔로우한 삭제되지 않은 대상만 조회
    @Query("""
        SELECT f.followed.id
        FROM Follow f
        WHERE f.following.id = :followingId
          AND f.followed.id IN :ids
          AND f.followed.isDeleted = false
    """)
    List<Long> findFollowedIdsByFollowingAndFollowedIds(
            @Param("followingId") Long followingId,
            @Param("ids") List<Long> ids
    );
}
