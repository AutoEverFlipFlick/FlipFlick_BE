package com.flipflick.backend.api.debate.service;

import com.flipflick.backend.api.debate.dto.DebateCommentRequestDto;
import com.flipflick.backend.api.debate.dto.DebateCommentResponseDto;
import com.flipflick.backend.api.debate.entity.Debate;
import com.flipflick.backend.api.debate.entity.DebateComment;
import com.flipflick.backend.api.debate.repository.DebateCommentRepository;
import com.flipflick.backend.api.debate.repository.DebateRepository;
import com.flipflick.backend.api.member.entity.Member;
import com.flipflick.backend.api.member.repository.MemberRepository;
import com.flipflick.backend.common.exception.BadRequestException;
import com.flipflick.backend.common.exception.NotFoundException;
import com.flipflick.backend.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DebateCommentService {

    private final DebateCommentRepository debateCommentRepository;
    private final DebateRepository debateRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void addComment(Long memberId, DebateCommentRequestDto dto) {
        Debate debate = debateRepository.findById(dto.getDebateId())
                .orElseThrow(() -> new NotFoundException(ErrorStatus.DEBATE_NOT_FOUND.getMessage()));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOT_FOUND.getMessage()));

        DebateComment comment = DebateComment.builder()
                .content(dto.getContent())
                .debate(debate)
                .member(member)
                .build();

        debateCommentRepository.save(comment);
    }


    public List<DebateCommentResponseDto> getComments(Long debateId) {
        return debateCommentRepository.findByDebateIdAndIsDeletedFalseOrderByCreatedAtAsc(debateId)
                .stream()
                .map(DebateCommentResponseDto::from)
                .toList();
    }

    public void deleteComment(Long commentId, Long memberId) {
        DebateComment comment = debateCommentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.COMMENT_NOT_FOUND.getMessage()));

        if (!comment.getMember().getId().equals(memberId)) {
            throw new BadRequestException(ErrorStatus.DEBATE_COMMENT_DELETE_DENIED.getMessage());
        }

        comment.softDelete();
    }
}