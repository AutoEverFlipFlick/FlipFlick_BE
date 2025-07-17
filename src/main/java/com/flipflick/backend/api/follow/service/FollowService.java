package com.flipflick.backend.api.follow.service;

import com.flipflick.backend.api.follow.entity.Follow;
import com.flipflick.backend.api.follow.repository.FollowRepository;
import com.flipflick.backend.api.member.entity.Member;
import com.flipflick.backend.api.member.repository.MemberRepository;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Builder
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final MemberRepository memberRepository;

    // 팔로우
    @Transactional
    public void follow(String email, Long targetMemberId) {
        Member following = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보 없음"));
        Member followed = memberRepository.findById(targetMemberId)
                .orElseThrow(() -> new IllegalArgumentException("대상 회원 없음"));

        if (following.equals(followed)) {
            throw new IllegalArgumentException("자기 자신은 팔로우할 수 없습니다.");
        }

        if (followRepository.existsByFollowingAndFollowed(following, followed)) {
            throw new IllegalStateException("이미 팔로우 중입니다.");
        }

        Follow follow = Follow.builder()
                .following(following)
                .followed(followed)
                .build();

        followRepository.save(follow);
    }

    // 언팔로우
    @Transactional
    public void unfollow(String email, Long targetMemberId) {
        Member following = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보 없음"));
        Member followed = memberRepository.findById(targetMemberId)
                .orElseThrow(() -> new IllegalArgumentException("대상 회원 없음"));

        Follow follow = followRepository.findByFollowingAndFollowed(following, followed)
                .orElseThrow(() -> new IllegalArgumentException("팔로우 정보 없음"));

        followRepository.delete(follow);
    }

    //팔로워 수
    @Transactional(readOnly = true)
    public long getFollowerCount(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));
        return followRepository.countByFollowed(member);
    }

    //팔로잉 수
    @Transactional(readOnly = true)
    public long getFollowingCount(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));
        return followRepository.countByFollowing(member);
    }

    @Transactional(readOnly = true)
    public List<Member> getFollowers(Long memberId) {
        Member followed = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원 없음"));

        return followRepository.findAllByFollowed(followed)
                .stream()
                .map(Follow::getFollowing)  // 나를 팔로우한 사람
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Member> getFollowings(Long memberId) {
        Member following = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원 없음"));

        return followRepository.findAllByFollowing(following)
                .stream()
                .map(Follow::getFollowed)  // 내가 팔로우한 사람
                .toList();
    }


    // 팔로우 여부 확인
    @Transactional(readOnly = true)
    public boolean isFollowing(String email, Long targetMemberId) {
        Member following = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보 없음"));
        Member followed = memberRepository.findById(targetMemberId)
                .orElseThrow(() -> new IllegalArgumentException("대상 회원 없음"));

        return followRepository.existsByFollowingAndFollowed(following, followed);
    }
}
