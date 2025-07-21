package com.flipflick.backend.api.follow.controller;

import com.flipflick.backend.api.alarm.service.AlarmService;
import com.flipflick.backend.api.follow.service.FollowService;
import com.flipflick.backend.api.member.dto.MemberResponseDto;
import com.flipflick.backend.api.member.entity.Member;
import com.flipflick.backend.api.member.service.MemberService;
import com.flipflick.backend.common.config.security.SecurityMember;
import com.flipflick.backend.common.response.ApiResponse;
import com.flipflick.backend.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
    private final AlarmService alarmService;
    private final MemberService memberService;

    // 팔로우
    @Operation(summary = "팔로우 API", description = "팔로우합니다.")
    @PostMapping("/{followedId}")
    public ResponseEntity<ApiResponse<Void>> follow(
            @AuthenticationPrincipal SecurityMember securityMember,
            @PathVariable Long followedId
    ) {
        // 1) 팔로우 처리
        followService.follow(securityMember.getEmail(), followedId);

        // 2) 팔로워의 닉네임 조회
        //    principal.getId() 는 SecurityMember 에서 꺼낸 유저 고유 ID
        MemberResponseDto me = memberService.getMemberById(securityMember.getId());
        String followerNick = me.getNickname();

        // 3) 알람 생성
        String content = followerNick + "님이 회원님을 팔로우했습니다.";
        alarmService.createAlarm(followedId, content);

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
    public ResponseEntity<ApiResponse<Page<MemberResponseDto>>> getFollowers(
            @PathVariable Long memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<Member> followers = followService.getFollowers(memberId, page, size);
        Page<MemberResponseDto> dtoPage = followers.map(MemberResponseDto::of);
        return ApiResponse.success(SuccessStatus.SEND_FOLLOWER_LIST_SUCCESS, dtoPage);
    }

    // 팔로잉 목록 조회
    @Operation(summary = "팔로우 리스트 조회 API", description = "팔로우 리스트를 조회합니다.")
    @GetMapping("/{memberId}/following")
    public ResponseEntity<ApiResponse<Page<MemberResponseDto>>> getFollowings(
            @PathVariable Long memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<Member> followings = followService.getFollowings(memberId, page, size);
        Page<MemberResponseDto> dtoPage = followings.map(MemberResponseDto::of);
        return ApiResponse.success(SuccessStatus.SEND_FOLLOWING_LIST_SUCCESS, dtoPage);
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