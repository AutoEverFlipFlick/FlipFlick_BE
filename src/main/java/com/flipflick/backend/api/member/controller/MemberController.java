package com.flipflick.backend.api.member.controller;

import com.flipflick.backend.api.member.dto.*;
import com.flipflick.backend.api.member.entity.Member;
import com.flipflick.backend.api.member.service.MemberService;
import com.flipflick.backend.common.config.security.SecurityMember;
import com.flipflick.backend.common.response.ApiResponse;
import com.flipflick.backend.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/member")
@Tag(name="Member", description = "Member 관련 API 입니다.")
public class MemberController {

    private final MemberService memberService;

    @Operation(
            summary = "이메일 회원가입 API", description = "회원정보를 받아 사용자를 등록합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "회원가입 성공")
    })
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(@Valid @RequestBody MemberSignupRequestDto requestDto) {
        memberService.signup(requestDto);
        return ApiResponse.success_only(SuccessStatus.SEND_REGISTER_SUCCESS);
    }

    @Operation(
            summary = "이메일 로그인 API", description = "이메일과 비밀번호로 로그인 합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "로그인 성공")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(@RequestBody LoginRequestDto requestDto, HttpServletResponse response) {
        LoginResponseDto loginResponseDto = memberService.login(requestDto);

        Cookie cookie = new Cookie("refresh", loginResponseDto.getRefreshToken());
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24 * 7);
        response.addCookie(cookie);

        return ApiResponse.success(SuccessStatus.SEND_LOGIN_SUCCESS,loginResponseDto);
    }


    // 마이페이지 닉네임 변경
    @Operation(summary = "닉네임 변경 API", description = "사용자의 닉네임을 수정합니다.")
    @PutMapping("/nickname")
    public ResponseEntity<ApiResponse<Void>> updateNickname(
            @Valid @RequestBody NicknameUpdateRequestDto requestDto,
            @AuthenticationPrincipal SecurityMember securityMember
    ) {
        memberService.updateNickname(
                securityMember.getEmail(),
                requestDto.getNickname()
        );
        return ApiResponse.success(SuccessStatus.UPDATE_NICKNAME_SUCCESS, null);
    }


    // 마이페이지 비밀번호 변경
    @Operation(summary = "비밀번호 변경 API", description = "사용자의 비밀번호를 변경합니다.")
    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> updatePassword(
            @Valid @RequestBody PasswordUpdateRequestDto requestDto,
            @AuthenticationPrincipal SecurityMember securityMember
    ) {
        memberService.updatePassword(
                securityMember.getEmail(),
                requestDto
        );
        return ApiResponse.success(SuccessStatus.UPDATE_PASSWORD_SUCCESS, null);
    }

    // 회원 정보 조회
    @GetMapping("/user-info")
    public ResponseEntity<?> getMemberInfo(@AuthenticationPrincipal SecurityMember securityMember) {

        Member member = memberService.getMemberInfo(securityMember.getId());
        MemberResponseDto memberResponseDto = MemberResponseDto.of(member);
        return ApiResponse.success(SuccessStatus.SEND_LOGIN_SUCCESS, memberResponseDto);
    }


}
