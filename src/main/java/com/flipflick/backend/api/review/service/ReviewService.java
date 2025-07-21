package com.flipflick.backend.api.review.service;

import com.flipflick.backend.api.member.entity.Member;
import com.flipflick.backend.api.member.repository.MemberRepository;
import com.flipflick.backend.api.movie.entity.Movie;
import com.flipflick.backend.api.movie.repository.MovieRepository;
import com.flipflick.backend.api.review.dto.ReviewRequestDto;
import com.flipflick.backend.api.review.dto.ReviewResponseDto;
import com.flipflick.backend.api.review.entity.LikeHateType;
import com.flipflick.backend.api.review.entity.Review;
import com.flipflick.backend.api.review.entity.ReviewLikeHate;
import com.flipflick.backend.api.review.repository.ReviewLikeHateRepository;
import com.flipflick.backend.api.review.repository.ReviewRepository;
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
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewLikeHateRepository reviewLikeHateRepository;
    private final MemberRepository memberRepository;
    private final MovieRepository movieRepository;

    // 1. 리뷰 작성
    @Transactional
    public ReviewResponseDto.Create createReview(Long memberId, ReviewRequestDto.Create request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOT_FOUND.getMessage()));

        Movie movie = movieRepository.findByTmdbId(request.getTmdbId())
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_REGISTER_MOVIE_EXCEPTION.getMessage()));

        // 중복 리뷰 체크
        if (reviewRepository.findByMemberIdAndMovieTmdbIdAndIsDeletedFalse(memberId, request.getTmdbId()).isPresent()) {
            throw new BadRequestException(ErrorStatus.REVIEW_ALREADY_EXISTS.getMessage());
        }

        // 별점 유효성 체크 (0.5 단위)
        if (!isValidStarRating(request.getStar())) {
            throw new BadRequestException(ErrorStatus.REVIEW_INVALID_STAR_RATING.getMessage());
        }

        Review review = Review.builder()
                .member(member)
                .movie(movie)
                .content(request.getContent())
                .star(request.getStar())
                .spoiler(request.getSpoiler())
                .build();

        review = reviewRepository.save(review);

        // 영화 평점 업데이트
        updateMovieVoteAverage(movie.getTmdbId());

        return ReviewResponseDto.Create.builder()
                .reviewId(review.getId())
                .content(review.getContent())
                .star(review.getStar())
                .build();
    }

    // 2. 리뷰 수정
    @Transactional
    public ReviewResponseDto.Update updateReview(Long memberId, Long reviewId, ReviewRequestDto.Update request) {
        Review review = reviewRepository.findByIdAndIsDeletedFalse(reviewId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.REVIEW_NOT_FOUND.getMessage()));

        if (!review.getMember().getId().equals(memberId)) {
            throw new BadRequestException(ErrorStatus.REVIEW_UPDATE_DENIED.getMessage());
        }

        if (!isValidStarRating(request.getStar())) {
            throw new BadRequestException(ErrorStatus.REVIEW_INVALID_STAR_RATING.getMessage());
        }

        review.updateReview(request.getContent(), request.getStar(), request.getSpoiler());

        // 영화 평점 업데이트
        updateMovieVoteAverage(review.getMovie().getTmdbId());

        return ReviewResponseDto.Update.builder()
                .reviewId(review.getId())
                .content(review.getContent())
                .star(review.getStar())
                .build();
    }

    // 3. 리뷰 삭제
    @Transactional
    public ReviewResponseDto.Delete deleteReview(Long memberId, Long reviewId) {
        Review review = reviewRepository.findByIdAndIsDeletedFalse(reviewId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.REVIEW_NOT_FOUND.getMessage()));

        if (!review.getMember().getId().equals(memberId)) {
            throw new BadRequestException(ErrorStatus.REVIEW_DELETE_DENIED.getMessage());
        }

        review.softDelete();

        // 영화 평점 업데이트
        updateMovieVoteAverage(review.getMovie().getTmdbId());

        return ReviewResponseDto.Delete.builder()
                .reviewId(review.getId())
                .message("리뷰가 삭제되었습니다.")
                .build();
    }

    // 4. 리뷰 목록 조회 (최신순)
    public ReviewResponseDto.PageResponse getReviewsByLatest(Long tmdbId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Review> reviewPage = reviewRepository.findByMovieTmdbIdAndIsDeletedFalseOrderByCreatedAtDesc(tmdbId, pageable);

        Page<ReviewResponseDto.Detail> detailPage = reviewPage.map(this::convertToDetail);
        return ReviewResponseDto.PageResponse.from(detailPage);
    }

    // 5. 리뷰 목록 조회 (인기순)
    public ReviewResponseDto.PageResponse getReviewsByPopularity(Long tmdbId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Review> reviewPage = reviewRepository.findByMovieTmdbIdAndIsDeletedFalseOrderByLikeCntDesc(tmdbId, pageable);

        Page<ReviewResponseDto.Detail> detailPage = reviewPage.map(this::convertToDetail);
        return ReviewResponseDto.PageResponse.from(detailPage);
    }

    // 6. 리뷰 좋아요/싫어요 토글
    @Transactional
    public ReviewResponseDto.LikeHate toggleLikeHate(Long memberId, ReviewRequestDto.LikeHate request) {
        Review review = reviewRepository.findByIdAndIsDeletedFalse(request.getReviewId())
                .orElseThrow(() -> new NotFoundException(ErrorStatus.REVIEW_NOT_FOUND.getMessage()));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOT_FOUND.getMessage()));

        // 자신의 리뷰에 좋아요/싫어요 불가
        if (review.getMember().getId().equals(memberId)) {
            throw new BadRequestException(ErrorStatus.REVIEW_SELF_LIKE_HATE_DENIED.getMessage());
        }

        LikeHateType type = LikeHateType.valueOf(request.getType());

        // 기존 좋아요/싫어요 조회
        ReviewLikeHate existingLikeHate = reviewLikeHateRepository
                .findByReviewIdAndMemberId(request.getReviewId(), memberId)
                .orElse(null);

        String message;

        if (existingLikeHate != null) {
            if (existingLikeHate.getType() == type) {
                // 같은 타입이면 취소
                reviewLikeHateRepository.delete(existingLikeHate);
                updateReviewLikeHateCount(review, type, false);
                message = type == LikeHateType.LIKE ? "좋아요가 취소되었습니다." : "싫어요가 취소되었습니다.";
            } else {
                // 다른 타입이면 변경
                updateReviewLikeHateCount(review, existingLikeHate.getType(), false); // 기존 것 감소
                reviewLikeHateRepository.delete(existingLikeHate);

                ReviewLikeHate newLikeHate = ReviewLikeHate.builder()
                        .review(review)
                        .member(member)
                        .type(type)
                        .build();
                reviewLikeHateRepository.save(newLikeHate);

                updateReviewLikeHateCount(review, type, true); // 새로운 것 증가
                message = type == LikeHateType.LIKE ? "좋아요로 변경되었습니다." : "싫어요로 변경되었습니다.";
            }
        } else {
            // 새로 추가
            ReviewLikeHate newLikeHate = ReviewLikeHate.builder()
                    .review(review)
                    .member(member)
                    .type(type)
                    .build();
            reviewLikeHateRepository.save(newLikeHate);

            updateReviewLikeHateCount(review, type, true);
            message = type == LikeHateType.LIKE ? "좋아요가 추가되었습니다." : "싫어요가 추가되었습니다.";
        }

        return ReviewResponseDto.LikeHate.builder()
                .reviewId(review.getId())
                .type(type.name())
                .message(message)
                .likeCnt(review.getLikeCnt())
                .hateCnt(review.getHateCnt())
                .build();
    }

    // 7. 닉네임으로 리뷰 목록 조회 (최신순)
    public ReviewResponseDto.PageResponse getReviewsByNicknameLatest(String nickname, int page, int size) {
        // 닉네임으로 사용자 존재 확인
        Member member = memberRepository.findByNickname(nickname)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOT_FOUND.getMessage()));

        Pageable pageable = PageRequest.of(page, size);
        Page<Review> reviewPage = reviewRepository.findByMemberNicknameAndIsDeletedFalseOrderByCreatedAtDesc(nickname, pageable);

        Page<ReviewResponseDto.Detail> detailPage = reviewPage.map(this::convertToDetail);
        return ReviewResponseDto.PageResponse.from(detailPage);
    }

    // 별점 유효성 체크 (0.5 단위)
    private boolean isValidStarRating(Double star) {
        if (star < 1.0 || star > 5.0) {
            return false;
        }
        return (star * 2) % 1 == 0; // 0.5 단위 체크
    }

    // 영화 평점 업데이트
    private void updateMovieVoteAverage(Long tmdbId) {
        Double averageStar = reviewRepository.calculateAverageStarByMovieTmdbId(tmdbId);
        if (averageStar != null) {
            Movie movie = movieRepository.findByTmdbId(tmdbId)
                    .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_REGISTER_MOVIE_EXCEPTION.getMessage()));
            movie.updateVoteAverage(averageStar);
        }
    }

    // 리뷰 좋아요/싫어요 카운트 업데이트
    private void updateReviewLikeHateCount(Review review, LikeHateType type, boolean increase) {
        if (type == LikeHateType.LIKE) {
            if (increase) {
                review.increaseLikeCnt();
            } else {
                review.decreaseLikeCnt();
            }
        } else {
            if (increase) {
                review.increaseHateCnt();
            } else {
                review.decreaseHateCnt();
            }
        }
    }

    // Review 엔티티를 Detail DTO로 변환
    private ReviewResponseDto.Detail convertToDetail(Review review) {
        return ReviewResponseDto.Detail.builder()
                .reviewId(review.getId())
                .memberId(review.getMember().getId())
                .content(review.getContent())
                .star(review.getStar())
                .spoiler(review.getSpoiler())
                .likeCnt(review.getLikeCnt())
                .hateCnt(review.getHateCnt())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .nickname(review.getMember().getNickname())
                .profileImage(review.getMember().getProfileImage())
                .popcorn(review.getMember().getPopcorn())
                .movieTitle(review.getMovie().getTitle())
                .posterImg(review.getMovie().getPosterImg())
                .build();
    }
}