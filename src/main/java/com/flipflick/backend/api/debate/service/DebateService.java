package com.flipflick.backend.api.debate.service;

import com.flipflick.backend.api.alarm.service.AlarmService;
import com.flipflick.backend.api.debate.dto.DebateRequestDto;
import com.flipflick.backend.api.debate.dto.DebateResponseDto;
import com.flipflick.backend.api.debate.entity.Debate;
import com.flipflick.backend.api.debate.entity.DebateLikeHate;
import com.flipflick.backend.api.debate.repository.DebateCommentRepository;
import com.flipflick.backend.api.debate.repository.DebateLikeHateRepository;
import com.flipflick.backend.api.debate.repository.DebateRepository;
import com.flipflick.backend.api.member.entity.Member;
import com.flipflick.backend.api.member.repository.MemberRepository;
import com.flipflick.backend.api.movie.entity.Movie;
import com.flipflick.backend.api.movie.repository.MovieRepository;
import com.flipflick.backend.api.review.entity.LikeHateType;
import com.flipflick.backend.common.exception.BadRequestException;
import com.flipflick.backend.common.exception.NotFoundException;
import com.flipflick.backend.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DebateService {

    private final DebateRepository debateRepository;
    private final DebateLikeHateRepository debateLikeHateRepository;
    private final MemberRepository memberRepository;
    private final MovieRepository movieRepository;
    private final AlarmService alarmService;
    private final DebateCommentRepository debateCommentRepository;

    // 1. 토론 작성
    @Transactional
    public DebateResponseDto.DebateCreate createDebate(Long memberId, DebateRequestDto.DebateCreate request) {
        Member member = memberRepository.findByIdAndIsDeletedFalse(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOT_FOUND.getMessage()));

        Movie movie = movieRepository.findByTmdbId(request.getTmdbId())
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_REGISTER_MOVIE_EXCEPTION.getMessage()));

        Debate debate = Debate.builder()
                .member(member)
                .movie(movie)
                .debateTitle(request.getDebateTitle())
                .content(request.getContent())
                .spoiler(request.getSpoiler())
                .likeCnt(0L)
                .hateCnt(0L)
                .isDeleted(false)
                .build();

        debate = debateRepository.save(debate);

        try {
            alarmService.createDebateWriteAlarmForFollowers(memberId, request.getDebateTitle());
        } catch (Exception e) {
            log.error("토론 작성 알림 전송 실패 - 사용자: {}, 제목: {}", member.getNickname(), request.getDebateTitle(), e);
        }

        return DebateResponseDto.DebateCreate.builder()
                .debateId(debate.getId())
                .debateTitle(debate.getDebateTitle())
                .content(debate.getContent())
                .build();
    }

    // 2. 토론 수정
    @Transactional
    public DebateResponseDto.DebateUpdate updateDebate(Long memberId, Long debateId, DebateRequestDto.DebateUpdate request) {
        Debate debate = debateRepository.findByIdAndIsDeletedFalse(debateId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.DEBATE_NOT_FOUND.getMessage()));

        if (!debate.getMember().getId().equals(memberId)) {
            throw new BadRequestException(ErrorStatus.DEBATE_UPDATE_DENIED.getMessage());
        }

        debate.updateDebate(request.getDebateTitle(), request.getContent(), request.getSpoiler());

        return DebateResponseDto.DebateUpdate.builder()
                .debateId(debate.getId())
                .debateTitle(debate.getDebateTitle())
                .content(debate.getContent())
                .build();
    }

    // 3. 토론 삭제
    @Transactional
    public DebateResponseDto.DebateDelete deleteDebate(Long memberId, Long debateId) {
        Debate debate = debateRepository.findByIdAndIsDeletedFalse(debateId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.DEBATE_NOT_FOUND.getMessage()));

        if (!debate.getMember().getId().equals(memberId)) {
            throw new BadRequestException(ErrorStatus.DEBATE_DELETE_DENIED.getMessage());
        }

        debate.softDelete();

        return DebateResponseDto.DebateDelete.builder()
                .debateId(debate.getId())
                .message("토론가 삭제되었습니다.")
                .build();
    }

    // 4. 토론 목록 조회 (최신순)
    public DebateResponseDto.DebatePageResponse getDebatesByLatest(Long tmdbId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Debate> debatePage = debateRepository.findByMovieTmdbIdAndIsDeletedFalseOrderByCreatedAtDesc(tmdbId, pageable);

        Page<DebateResponseDto.DebateDetail> detailPage = debatePage.map(this::convertToDetail);
        return DebateResponseDto.DebatePageResponse.from(detailPage);
    }

    // 5. 토론 목록 조회 (인기순)
    public DebateResponseDto.DebatePageResponse getDebatesByPopularity(Long tmdbId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Debate> debatePage = debateRepository.findByMovieTmdbIdAndIsDeletedFalseOrderByLikeCntDesc(tmdbId, pageable);

        Page<DebateResponseDto.DebateDetail> detailPage = debatePage.map(this::convertToDetail);
        return DebateResponseDto.DebatePageResponse.from(detailPage);
    }

    // 6. 토론 좋아요/싫어요 토글
    @Transactional
    public DebateResponseDto.DebateLikeHate toggleLikeHate(Long memberId, DebateRequestDto.DebateLikeHate request) {
        Debate debate = debateRepository.findByIdAndIsDeletedFalse(request.getDebateId())
                .orElseThrow(() -> new NotFoundException(ErrorStatus.DEBATE_NOT_FOUND.getMessage()));

        Member member = memberRepository.findByIdAndIsDeletedFalse(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOT_FOUND.getMessage()));

//
//        if (debate.getMember().getId().equals(memberId)) {
//            throw new BadRequestException(ErrorStatus.DEBATE_SELF_LIKE_HATE_DENIED.getMessage());
//        }

        LikeHateType type = LikeHateType.valueOf(request.getType());

        // 기존 좋아요/싫어요 조회
        DebateLikeHate existingLikeHate = debateLikeHateRepository
                .findByDebateIdAndMemberId(request.getDebateId(), memberId)
                .orElse(null);

        String message;

        if (existingLikeHate != null) {
            if (existingLikeHate.getType() == type) {
                // 같은 타입이면 취소
                debateLikeHateRepository.delete(existingLikeHate);
                updateDebateLikeHateCount(debate, type, false);
                message = type == LikeHateType.LIKE ? "좋아요가 취소되었습니다." : "싫어요가 취소되었습니다.";
            } else {
                // 다른 타입이면 변경
                updateDebateLikeHateCount(debate, existingLikeHate.getType(), false); // 기존 것 감소
                debateLikeHateRepository.delete(existingLikeHate);

                DebateLikeHate newLikeHate = DebateLikeHate.builder()
                        .debate(debate)
                        .member(member)
                        .type(type)
                        .build();
                debateLikeHateRepository.save(newLikeHate);

                updateDebateLikeHateCount(debate, type, true); // 새로운 것 증가
                message = type == LikeHateType.LIKE ? "좋아요로 변경되었습니다." : "싫어요로 변경되었습니다.";
                if(type == LikeHateType.LIKE){
                    alarmService.createAlarm(debate.getMember().getId(),"'"+debate.getDebateTitle()+"에 좋아요가 달렸습니다.");
                }
            }
        } else {
            // 새로 추가
            DebateLikeHate newLikeHate = DebateLikeHate.builder()
                    .debate(debate)
                    .member(member)
                    .type(type)
                    .build();
            debateLikeHateRepository.save(newLikeHate);

            updateDebateLikeHateCount(debate, type, true);
            message = type == LikeHateType.LIKE ? "좋아요가 추가되었습니다." : "싫어요가 추가되었습니다.";
            if(type == LikeHateType.LIKE){
                alarmService.createAlarm(debate.getMember().getId(),"'"+debate.getDebateTitle()+"에 좋아요가 달렸습니다.");
            }
        }

        return DebateResponseDto.DebateLikeHate.builder()
                .debateId(debate.getId())
                .type(type.name())
                .message(message)
                .likeCnt(debate.getLikeCnt())
                .hateCnt(debate.getHateCnt())
                .build();
    }

    // 7. 닉네임으로 토론 목록 조회 (최신순)
    public DebateResponseDto.DebatePageResponse getDebatesByNicknameLatest(String nickname, int page, int size) {
        // 닉네임으로 사용자 존재 확인
        Member member = memberRepository.findByNicknameAndIsDeletedFalse(nickname)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOT_FOUND.getMessage()));

        Pageable pageable = PageRequest.of(page, size);
        Page<Debate> debatePage = debateRepository.findByMemberNicknameAndIsDeletedFalseOrderByCreatedAtDesc(nickname, pageable);

        Page<DebateResponseDto.DebateDetail> detailPage = debatePage.map(this::convertToDetail);
        return DebateResponseDto.DebatePageResponse.from(detailPage);
    }

    // 토론 좋아요/싫어요 카운트 업데이트
    private void updateDebateLikeHateCount(Debate debate, LikeHateType type, boolean increase) {
        if (type == LikeHateType.LIKE) {
            if (increase) {
                debate.increaseLikeCnt();
            } else {
                debate.decreaseLikeCnt();
            }
        } else {
            if (increase) {
                debate.increaseHateCnt();
            } else {
                debate.decreaseHateCnt();
            }
        }
    }

    // Debate 엔티티를 Detail DTO로 변환 (목록 조회용)
    private DebateResponseDto.DebateDetail convertToDetail(Debate debate) {
        int commentCount = debateCommentRepository.countByDebateId(debate.getId());
        return DebateResponseDto.DebateDetail.builder()
                .debateId(debate.getId())
                .memberId(debate.getMember().getId())
                .debateTitle(debate.getDebateTitle())
                .content(debate.getContent())
                .spoiler(debate.getSpoiler())
                .likeCnt(debate.getLikeCnt())
                .hateCnt(debate.getHateCnt())
                .createdAt(debate.getCreatedAt())
                .updatedAt(debate.getUpdatedAt())
                .nickname(debate.getMember().getNickname())
                .profileImage(debate.getMember().getProfileImage())
                .popcorn(debate.getMember().getPopcorn())
                .movieTitle(debate.getMovie().getTitle())
                .tmdbId(debate.getMovie().getTmdbId())
                .commentCount(commentCount)
                .build();
    }

    private DebateResponseDto.DebateDetail convertToDetailWithMovie(Debate debate) {
        DebateResponseDto.DebateMovieInfo movieInfo = DebateResponseDto.DebateMovieInfo.builder()
                .tmdbId(debate.getMovie().getTmdbId())
                .title(debate.getMovie().getTitle())
                .posterImg(debate.getMovie().getPosterImg())
                .releaseDate(debate.getMovie().getReleaseDate())
                .rating(debate.getMovie().getVoteAverage())
                .build();

        return DebateResponseDto.DebateDetail.builder()
                .debateId(debate.getId())
                .memberId(debate.getMember().getId())
                .debateTitle(debate.getDebateTitle())
                .content(debate.getContent())
                .spoiler(debate.getSpoiler())
                .likeCnt(debate.getLikeCnt())
                .hateCnt(debate.getHateCnt())
                .createdAt(debate.getCreatedAt())
                .updatedAt(debate.getUpdatedAt())
                .nickname(debate.getMember().getNickname())
                .profileImage(debate.getMember().getProfileImage())
                .popcorn(debate.getMember().getPopcorn())
                .movieTitle(debate.getMovie().getTitle())
                .tmdbId(debate.getMovie().getTmdbId())
                .movie(movieInfo)
                .build();
    }

    // 특정 토론 조회
    public DebateResponseDto.DebateDetail getDebateById(Long debateId) {
        Debate debate = debateRepository.findByIdAndIsDeletedFalse(debateId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.DEBATE_NOT_FOUND.getMessage()));

        return convertToDetailWithMovie(debate);
    }

    // 특정 사용자의 특정 영화에 대한 토론 목록 조회 (최신순)
    public DebateResponseDto.DebatePageResponse getDebatesByMemberAndMovieLatest(Long memberId, Long tmdbId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Debate> debatePage = debateRepository.findByMemberIdAndMovieTmdbIdAndIsDeletedFalseOrderByCreatedAtDesc(memberId, tmdbId, pageable);

        Page<DebateResponseDto.DebateDetail> detailPage = debatePage.map(this::convertToDetail);
        return DebateResponseDto.DebatePageResponse.from(detailPage);
    }

    // 특정 사용자의 특정 영화에 대한 토론 목록 조회 (인기순)
    public DebateResponseDto.DebatePageResponse getDebatesByMemberAndMoviePopularity(Long memberId, Long tmdbId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Debate> debatePage = debateRepository.findByMemberIdAndMovieTmdbIdAndIsDeletedFalseOrderByLikeCntDesc(memberId, tmdbId, pageable);

        Page<DebateResponseDto.DebateDetail> detailPage = debatePage.map(this::convertToDetail);
        return DebateResponseDto.DebatePageResponse.from(detailPage);
    }

    // 특정 사용자의 토론 목록 조회 (최신순)
    public DebateResponseDto.DebatePageResponse getDebatesByMemberLatest(Long memberId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Debate> debatePage = debateRepository.findByMemberIdAndIsDeletedFalseOrderByCreatedAtDesc(memberId, pageable);

        Page<DebateResponseDto.DebateDetail> detailPage = debatePage.map(this::convertToDetail);
        return DebateResponseDto.DebatePageResponse.from(detailPage);
    }

    // 특정 사용자의 토론 목록 조회 (인기순)
    public DebateResponseDto.DebatePageResponse getDebatesByMemberPopularity(Long memberId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Debate> debatePage = debateRepository.findByMemberIdAndIsDeletedFalseOrderByLikeCntDesc(memberId, pageable);

        Page<DebateResponseDto.DebateDetail> detailPage = debatePage.map(this::convertToDetail);
        return DebateResponseDto.DebatePageResponse.from(detailPage);
    }

    // 닉네임으로 토론 목록 조회 (인기순)
    public DebateResponseDto.DebatePageResponse getDebatesByNicknamePopularity(String nickname, int page, int size) {
        // 닉네임으로 사용자 존재 확인
        Member member = memberRepository.findByNicknameAndIsDeletedFalse(nickname)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOT_FOUND.getMessage()));

        Pageable pageable = PageRequest.of(page, size);
        Page<Debate> debatePage = debateRepository.findByMemberNicknameAndIsDeletedFalseOrderByLikeCntDesc(nickname, pageable);

        Page<DebateResponseDto.DebateDetail> detailPage = debatePage.map(this::convertToDetail);
        return DebateResponseDto.DebatePageResponse.from(detailPage);
    }


}
