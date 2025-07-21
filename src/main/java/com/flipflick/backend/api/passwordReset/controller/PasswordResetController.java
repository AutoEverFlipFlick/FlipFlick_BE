package com.flipflick.backend.api.passwordReset.controller;

import com.flipflick.backend.api.admin.dto.DashboardStatResponseDto;
import com.flipflick.backend.api.passwordReset.dto.ResetPasswordRequestDto;
import com.flipflick.backend.api.passwordReset.dto.SendResetLinkRequestDto;
import com.flipflick.backend.api.passwordReset.service.PasswordResetService;
import com.flipflick.backend.common.response.ApiResponse;
import com.flipflick.backend.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/password-reset")
@Tag(name="Password-Reset", description = "Password-Reset 관련 API 입니다.")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    // 1. 비밀번호 재설정 링크 이메일 전송
    @Operation(summary = "비밀번호 재설정 링크 이메일 전송", description = "비밀번호 재설정 링크 이메일 전송")
    @PostMapping("/send-link")
    public ResponseEntity<ApiResponse<Void>> sendResetLink(@RequestBody SendResetLinkRequestDto request) {
        passwordResetService.sendResetLink(request.getEmail());
        return ApiResponse.success_only(SuccessStatus.PASSWORD_RESET_LINK_SENT);
    }

    // 2. 재설정 실행 (code + new password)
    @Operation(summary = "비밀번호 재설정", description = "비밀번호 재설정")
    @PutMapping
    public ResponseEntity<ApiResponse<Void>> resetPassword(@RequestBody ResetPasswordRequestDto request) {
        passwordResetService.resetPasswordWithCode(request.getCode(), request.getNewPassword());
        return ApiResponse.success_only(SuccessStatus.PASSWORD_RESET_SUCCESS);
    }


}