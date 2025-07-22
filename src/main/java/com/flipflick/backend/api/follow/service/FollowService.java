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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
        Member following = memberRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.FOLLOW_USER_INFO_NOT_FOUND.getMessage()));
        Member followed = memberRepository.findByIdAndIsDeletedFalse(targetMemberId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.TARGET_USER_NOT_FOUND.getMessage()));

        if (following.equals(followed)) {
            throw new BadRequestException(ErrorStatus.SELF_FOLLOW_NOT_ALLOWED.getMessage());
        }

        if (followRepository.existsByFollowingAndFollowedAndBothNotDeleted(following, followed)) {
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
        Member following = memberRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOT_FOUND.getMessage()));
        Member followed = memberRepository.findByIdAndIsDeletedFalse(targetMemberId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.TARGET_USER_NOT_FOUND.getMessage()));

        Follow follow = followRepository.findByFollowingAndFollowedAndBothNotDeleted(following, followed)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.FOLLOW_NOT_FOUND.getMessage()));

        followRepository.delete(follow);
    }

    //팔로워 수
    @Transactional(readOnly = true)
    public long getFollowerCount(Long memberId) {
        Member member = memberRepository.findByIdAndIsDeletedFalse(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOT_FOUND.getMessage()));
        return followRepository.countByFollowedAndFollowingIsDeletedFalse(member);
    }

    //팔로잉 수
    @Transactional(readOnly = true)
    public long getFollowingCount(Long memberId) {
        Member member = memberRepository.findByIdAndIsDeletedFalse(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOT_FOUND.getMessage()));
        return followRepository.countByFollowingAndFollowedIsDeletedFalse(member);
    }

    // 팔로워
    @Transactional(readOnly = true)
    public Page<Member> getFollowers(Long memberId, int page, int size) {
        Member followed = memberRepository.findByIdAndIsDeletedFalse(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOT_FOUND.getMessage()));

        Pageable pageable = PageRequest.of(page, size);
        return followRepository.findAllByFollowedAndFollowingIsDeletedFalse(followed, pageable)
                .map(Follow::getFollowing);
    }

    // 팔로잉
    @Transactional(readOnly = true)
    public Page<Member> getFollowings(Long memberId, int page, int size) {
        Member following = memberRepository.findByIdAndIsDeletedFalse(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOT_FOUND.getMessage()));

        Pageable pageable = PageRequest.of(page, size);
        return followRepository.findAllByFollowingAndFollowedIsDeletedFalse(following, pageable)
                .map(Follow::getFollowed);
    }

    // 팔로우 여부 확인
    @Transactional(readOnly = true)
    public boolean isFollowing(String email, Long targetMemberId) {
        Member following = memberRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.FOLLOW_USER_INFO_NOT_FOUND.getMessage()));
        Member followed = memberRepository.findByIdAndIsDeletedFalse(targetMemberId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.TARGET_USER_NOT_FOUND.getMessage()));

        return followRepository.existsByFollowingAndFollowedAndBothNotDeleted(following, followed);
    }
}
