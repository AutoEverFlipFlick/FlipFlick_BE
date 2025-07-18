package com.flipflick.backend.api.follow.service;

import com.flipflick.backend.api.follow.entity.Follow;
import com.flipflick.backend.api.follow.repository.FollowRepository;
import com.flipflick.backend.api.member.entity.Member;
import com.flipflick.backend.api.member.repository.MemberRepository;
import com.flipflick.backend.common.exception.BadRequestException;
import com.flipflick.backend.common.exception.NotFoundException;
import com.flipflick.backend.common.response.ErrorStatus;
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
                .orElseThrow(() -> new NotFoundException(ErrorStatus.FOLLOW_USER_INFO_NOT_FOUND.getMessage()));
        Member followed = memberRepository.findById(targetMemberId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.TARGET_USER_NOT_FOUND.getMessage()));

        if (following.equals(followed)) {
            throw new BadRequestException(ErrorStatus.SELF_FOLLOW_NOT_ALLOWED.getMessage());
        }

        if (followRepository.existsByFollowingAndFollowed(following, followed)) {
            throw new BadRequestException(ErrorStatus.FOLLOW_ALREADY_EXISTS.getMessage());
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
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOT_FOUND.getMessage()));
        Member followed = memberRepository.findById(targetMemberId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.TARGET_USER_NOT_FOUND.getMessage()));

        Follow follow = followRepository.findByFollowingAndFollowed(following, followed)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.FOLLOW_NOT_FOUND.getMessage()));

        followRepository.delete(follow);
    }

    //팔로워 수
    @Transactional(readOnly = true)
    public long getFollowerCount(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOT_FOUND.getMessage()));
        return followRepository.countByFollowed(member);
    }

    //팔로잉 수
    @Transactional(readOnly = true)
    public long getFollowingCount(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOT_FOUND.getMessage()));
        return followRepository.countByFollowing(member);
    }

    // 팔로워
    @Transactional(readOnly = true)
    public List<Member> getFollowers(Long memberId) {
        Member followed = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOT_FOUND.getMessage()));;

        return followRepository.findAllByFollowed(followed)
                .stream()
                .map(Follow::getFollowing)  // 나를 팔로우한 사람
                .toList();
    }

    // 팔로잉
    @Transactional(readOnly = true)
    public List<Member> getFollowings(Long memberId) {
        Member following = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOT_FOUND.getMessage()));

        return followRepository.findAllByFollowing(following)
                .stream()
                .map(Follow::getFollowed)  // 내가 팔로우한 사람
                .toList();
    }

    // 팔로우 여부 확인
    @Transactional(readOnly = true)
    public boolean isFollowing(String email, Long targetMemberId) {
        Member following = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.FOLLOW_USER_INFO_NOT_FOUND.getMessage()));
        Member followed = memberRepository.findById(targetMemberId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.TARGET_USER_NOT_FOUND.getMessage()));

        return followRepository.existsByFollowingAndFollowed(following, followed);
    }
}
