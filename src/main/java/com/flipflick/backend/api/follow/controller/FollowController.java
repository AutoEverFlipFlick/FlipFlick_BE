package com.flipflick.backend.api.follow.controller;

import com.flipflick.backend.api.follow.service.FollowService;
import com.flipflick.backend.api.member.dto.MemberResponseDto;
import com.flipflick.backend.api.member.entity.Member;
import com.flipflick.backend.common.config.security.SecurityMember;
import com.flipflick.backend.common.response.ApiResponse;
import com.flipflick.backend.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/follow")
@Tag(name = "Follow", description = "팔로우 관련 API 입니다.")
public class FollowController {

    private final FollowService followService;

    // 팔로우
    @Operation(summary = "팔로우 API", description = "팔로우합니다.")
    @PostMapping("/{followedId}")
    public ResponseEntity<ApiResponse<Void>> follow(
            @AuthenticationPrincipal SecurityMember securityMember,
            @PathVariable Long followedId
    ) {
        followService.follow(securityMember.getEmail(), followedId);
        return ApiResponse.success_only(SuccessStatus.SEND_FOLLOW_SUCCESS);
    }

    // 언팔로우
    @Operation(summary = "언팔로우 API", description = "언팔로우합니다.")
    @DeleteMapping("/{followedId}")
    public ResponseEntity<ApiResponse<Void>> unfollow(
            @AuthenticationPrincipal SecurityMember securityMember,
            @PathVariable Long followedId
    ) {
        followService.unfollow(securityMember.getEmail(), followedId);
        return ApiResponse.success_only(SuccessStatus.SEND_UNFOLLOW_SUCCESS);
    }

    // 팔로우 수
    @Operation(summary = "팔로우 수 조회 API", description = "팔로워 및 팔로잉 수를 조회합니다.")
    @GetMapping("/count/{memberId}")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getFollowCounts(@PathVariable Long memberId) {
        long followerCount = followService.getFollowerCount(memberId);
        long followingCount = followService.getFollowingCount(memberId);

        Map<String, Long> counts = new HashMap<>();
        counts.put("followerCount", followerCount);
        counts.put("followingCount", followingCount);

        return ApiResponse.success(SuccessStatus.SEND_CHECK_FOLLOW_SUCCESS, counts);
    }

    // 팔로워 목록 조회
    @Operation(summary = "팔로워 리스트 조회 API", description = "팔로워 리스트를 조회합니다.")
    @GetMapping("/{memberId}/follower")
    public ResponseEntity<ApiResponse<List<MemberResponseDto>>> getFollowers(@PathVariable Long memberId) {
        List<Member> followers = followService.getFollowers(memberId);
        List<MemberResponseDto> result = followers.stream()
                .map(MemberResponseDto::of)
                .toList();

        return ApiResponse.success(SuccessStatus.SEND_FOLLOWER_LIST_SUCCESS, result);
    }

    // 팔로잉 목록 조회
    @Operation(summary = "팔로우 리스트 조회 API", description = "팔로우 리스트를 조회합니다.")
    @GetMapping("/{memberId}/following")
    public ResponseEntity<ApiResponse<List<MemberResponseDto>>> getFollowings(@PathVariable Long memberId) {
        List<Member> followings = followService.getFollowings(memberId);
        List<MemberResponseDto> result = followings.stream()
                .map(MemberResponseDto::of)
                .toList();

        return ApiResponse.success(SuccessStatus.SEND_FOLLOWING_LIST_SUCCESS, result);
    }

    // 팔로우 여부 확인
    @Operation(summary = "팔로우 여부 확인 API", description = "특정 회원을 팔로우 중인지 확인합니다.")
    @GetMapping("/check")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkFollowStatus(
            @AuthenticationPrincipal SecurityMember securityMember,
            @RequestParam Long targetId
    ) {
        boolean isFollowing = followService.isFollowing(securityMember.getEmail(), targetId);
        Map<String, Boolean> result = new HashMap<>();
        result.put("isFollowing", isFollowing);
        return ApiResponse.success(SuccessStatus.SEND_CHECK_FOLLOW_SUCCESS, result);
    }
}