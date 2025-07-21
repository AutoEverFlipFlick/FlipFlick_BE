package com.flipflick.backend.api.debate.dto;

import com.flipflick.backend.api.debate.entity.DebateComment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DebateCommentResponseDto {
    private Long id;
    private String content;
    private String memberNickname;
    private LocalDateTime createdAt;

    public static DebateCommentResponseDto from(DebateComment comment) {
        return DebateCommentResponseDto.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .memberNickname(comment.getMember().getNickname())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
