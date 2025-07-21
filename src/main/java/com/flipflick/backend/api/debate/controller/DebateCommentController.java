package com.flipflick.backend.api.debate.controller;

import com.flipflick.backend.api.debate.dto.DebateCommentRequestDto;
import com.flipflick.backend.api.debate.dto.DebateCommentResponseDto;
import com.flipflick.backend.api.debate.service.DebateCommentService;
import com.flipflick.backend.common.config.security.SecurityMember;
import com.flipflick.backend.common.response.ApiResponse;
import com.flipflick.backend.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/debate/comments")
@Tag(name = "Debate-Comment", description = "토론 댓글 API")
public class DebateCommentController {

    private final DebateCommentService debateCommentService;

    @Operation(summary = "댓글 등록 API", description = "해당되는 토론에 댓글 등록")
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> addComment(@Valid @RequestBody DebateCommentRequestDto dto,
                                                        @AuthenticationPrincipal SecurityMember member) {
        debateCommentService.addComment(member.getId(), dto);
        return ApiResponse.success_only(SuccessStatus.SEND_DEBATE_COMMENT_CREATE_SUCCESS);
    }

    @Operation(summary = "댓글 목록 조회 API", description = "해당되는 토론의 댓글 목록 조회")
    @GetMapping("/{debateId}")
    public ResponseEntity<ApiResponse<List<DebateCommentResponseDto>>> getComments(@PathVariable Long debateId) {
        List<DebateCommentResponseDto> debateCommentResponseDtos = debateCommentService.getComments(debateId);
        return ApiResponse.success(SuccessStatus.SEND_DEBATE_COMMENT_CREATE_SUCCESS, debateCommentResponseDtos);
    }

    @Operation(summary = "댓글 삭제 API", description = "해당되는 댓글 삭제")
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable Long commentId,
                                           @AuthenticationPrincipal SecurityMember member) {
        debateCommentService.deleteComment(commentId, member.getId());
        return ApiResponse.success_only(SuccessStatus.SEND_DEBATE_COMMENT_DELETE_SUCCESS);
    }
}