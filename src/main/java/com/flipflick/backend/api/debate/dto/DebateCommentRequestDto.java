package com.flipflick.backend.api.debate.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
public class DebateCommentRequestDto {
    private Long debateId;

    @Size(max = 200, message = "댓글은 200자 이하로 작성해주세요.")
    private String content;
}
