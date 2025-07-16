package com.flipflick.backend.api.member.controller;

import com.flipflick.backend.api.member.dto.LoginRequestDto;
import com.flipflick.backend.api.member.dto.LoginResponseDto;
import com.flipflick.backend.api.member.dto.MemberSignupRequestDto;
import com.flipflick.backend.api.member.service.MemberService;
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


}
