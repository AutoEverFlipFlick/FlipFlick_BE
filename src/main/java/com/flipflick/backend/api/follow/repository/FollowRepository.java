package com.flipflick.backend.api.follow.repository;

import com.flipflick.backend.api.follow.entity.Follow;
import com.flipflick.backend.api.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

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
}