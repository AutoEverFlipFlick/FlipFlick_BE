package com.flipflick.backend.api.follow.repository;

import com.flipflick.backend.api.follow.entity.Follow;
import com.flipflick.backend.api.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    boolean existsByFollowingAndFollowed(Member following, Member followed);
    Optional<Follow> findByFollowingAndFollowed(Member following, Member followed);
    long countByFollowed(Member followed);
    long countByFollowing(Member following);
    // 특정 회원을 팔로우하는 사람들 (팔로워 목록)
    List<Follow> findAllByFollowed(Member followed);
    // 특정 회원이 팔로우하는 사람들 (팔로잉 목록)
    List<Follow> findAllByFollowing(Member following);

    // 수정: 특정 날짜 끝 시점의 팔로우 수 계산
    @Query("SELECT COUNT(f) FROM Follow f WHERE f.followed.id = :memberId " +
            "AND f.createdAt < :endOfDay")
    Integer countFollowsByMemberIdUntilDate(@Param("memberId") Long memberId,
                                            @Param("endOfDay") LocalDateTime endOfDay);

    // 여러 회원에 대한 팔로워 수를 한 번에 조회
    @Query("SELECT f.followed.id, COUNT(f) " +
            "FROM Follow f " +
            "WHERE f.followed.id IN :ids " +
            "GROUP BY f.followed.id")
    List<Object[]> countFollowersByFollowedIds(@Param("ids") List<Long> ids);

    // 로그인 유저가 여러 회원을 팔로우하고 있는지 한 번에 조회
    @Query("SELECT f.followed.id " +
            "FROM Follow f " +
            "WHERE f.following.id = :followingId " +
            "  AND f.followed.id IN :ids")
    List<Long> findFollowedIdsByFollowingAndFollowedIds(
            @Param("followingId") Long followingId,
            @Param("ids") List<Long> ids
    );
}