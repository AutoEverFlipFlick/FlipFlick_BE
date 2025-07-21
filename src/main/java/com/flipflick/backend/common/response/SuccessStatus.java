package com.flipflick.backend.common.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum SuccessStatus {

    /**
     * 200
     */
    SEND_REGISTER_SUCCESS(HttpStatus.OK,"회원가입 성공"),
    SEND_LOGIN_SUCCESS(HttpStatus.OK, "로그인 성공"),
    SEND_REISSUE_TOKEN_SUCCESS(HttpStatus.OK,"토큰 재발급 성공"),
    SEND_HEALTH_SUCCESS(HttpStatus.OK,"서버 상태 OK"),
    SEND_MOVIE_DETAIL_SUCCESS(HttpStatus.OK,"영화 상세 조회 성공"),
    SEND_MOVIE_LIST_SUCCESS(HttpStatus.OK,"영화 리스트 조회 성공"),
    SEND_PLAYLIST_LIST_SUCCESS(HttpStatus.OK,"플레이리스트 목록 조회 성공"),
    SEND_PLAYLIST_DETAIL_SUCCESS(HttpStatus.OK,"플레이리스트 상세 조회 성공"),
    SEND_PLAYLIST_CREATE_SUCCESS(HttpStatus.OK,"플레이리스트 생성 성공"),
    SEND_PLAYLIST_BOOKMARK_SUCCESS(HttpStatus.OK,"플레이리스트 북마크 처리 성공"),
    SEND_PLAYLIST_UPDATE_SUCCESS(HttpStatus.OK,"플레이리스트 수정 성공"),
    SEND_PLAYLIST_DELETE_SUCCESS(HttpStatus.OK,"플레이리스트 삭제 성공"),
    SEND_PLAYLIST_SEARCH_SUCCESS(HttpStatus.OK,"플레이리스트 검색 성공"),
    SEND_PLAYLIST_BOOKMARK_LIST_SUCCESS(HttpStatus.OK,"북마크 플레이리스트 ID 목록 조회 성공"),
    SEND_CAST_LIST_SUCCESS(HttpStatus.OK,"배우 리스트 조회 성공"),
    SEND_CAST_DETAIL_SUCCESS(HttpStatus.OK,"배우 상세 조회 성공"),
    UPDATE_NICKNAME_SUCCESS(HttpStatus.OK,"닉네임 변경 성공"),
    UPDATE_PASSWORD_SUCCESS(HttpStatus.OK,"비밀번호 변경 성공"),
    IMAGE_UPLOAD_SUCCESS(HttpStatus.OK, "이미지 업로드 성공"),
    SEND_KAKA_LOGIN_SUCCESS(HttpStatus.OK, "카카오 로그인 성공"),
    SEND_NAVER_LOGIN_SUCCESS(HttpStatus.OK, "네이버 로그인 성공"),
    UPDATE_PROFILE_IMAGE_SUCCESS(HttpStatus.OK,"프로필 이미지 변경 성공"),
    SEND_FOLLOW_SUCCESS(HttpStatus.OK,"팔로우 성공"),
    SEND_UNFOLLOW_SUCCESS(HttpStatus.OK,"언팔로우 성공"),
    SEND_CHECK_FOLLOW_SUCCESS(HttpStatus.OK,"팔로우 수 조회 성공"),
    SEND_FOLLOWER_LIST_SUCCESS(HttpStatus.OK,"팔로워 리스트 조회 성공"),
    SEND_FOLLOWING_LIST_SUCCESS(HttpStatus.OK,"팔로잉 리스트 조회 성공"),
    REISSUE_SUCCESS(HttpStatus.OK, "access토큰 재발급 성공"),
    SEND_REVIEW_CREATE_SUCCESS(HttpStatus.OK, "리뷰 작성 성공"),
    SEND_REVIEW_UPDATE_SUCCESS(HttpStatus.OK, "리뷰 수정 성공"),
    SEND_REVIEW_DELETE_SUCCESS(HttpStatus.OK, "리뷰 삭제 성공"),
    SEND_REVIEW_LIST_SUCCESS(HttpStatus.OK, "리뷰 목록 조회 성공"),
    SEND_REVIEW_LIKE_HATE_SUCCESS(HttpStatus.OK, "리뷰 좋아요/싫어요 처리 성공"),
    SEND_POPCORN_SCORE_SUCCESS(HttpStatus.OK, "팝콘지수 조회 성공"),
    SEND_POPCORN_SCORE_RECALCULATE_SUCCESS(HttpStatus.OK, "팝콘지수 재계산 성공"),
    CHECK_EMAIL_DUPLICATE(HttpStatus.OK, "이메일 중복 검사 결과입니다."),
    CHECK_NICKNAME_DUPLICATE(HttpStatus.OK, "닉네임 중복 검사 결과입니다."),
    IMAGE_DELETE_SUCCESS(HttpStatus.OK,"이미지 삭제 성공"),
    SEND_MOVIE_BOOKMARK_SUCCESS(HttpStatus.OK,"영화 찜 토글 성공"),
    SEND_MOVIE_BOOKMARK_LIST_SUCCESS(HttpStatus.OK,"찜 영화 목록 조회 성공"),
    SEND_MOVIE_WATCHED_SUCCESS(HttpStatus.OK,"영화 봤어요 토글 성공"),
    SEND_MOVIE_WATCHED_LIST_SUCCESS(HttpStatus.OK,"본 영화 목록 조회 성공"),
    UPDATE_SOCIAL_INFO_SUCCESS(HttpStatus.OK,"소셜 로그인 정보 설정 성공"),
    SEND_MOVIE_LIKE_HATE_SUCCESS(HttpStatus.OK,"좋아요, 싫어요 토글 성공"),
    SEND_MOVIE_LIKE_LIST_SUCCESS(HttpStatus.OK,"좋아요 한 영화 목록 조회 성공"),
    DASHBOARD_STAT_READ_SUCCESS(HttpStatus.OK, "대시보드 통계 조회 성공"),
    POPCORN_GRADE_STAT_READ_SUCCESS(HttpStatus.OK, "팝콘 등급 분포 조회 성공"),
    TOP_MOVIES_BY_REVIEW_SUCCESS(HttpStatus.OK, "리뷰 많은 영화 Top 5 조회 성공"),
    MEMBER_LIST_SUCCESS(HttpStatus.OK, "회원 목록 조회 성공"),
    MEMBER_STATUS_UPDATE_SUCCESS(HttpStatus.OK, "회원 상태 변경 성공"),
    SEND_MEMBER_LIST_SUCCESS(HttpStatus.OK,"사용자 리스트 조회 성공"),
    SEND_DEBATE_CREATE_SUCCESS(HttpStatus.OK, "토론 작성 성공"),
    SEND_DEBATE_UPDATE_SUCCESS(HttpStatus.OK, "토론 수정 성공"),
    SEND_DEBATE_DELETE_SUCCESS(HttpStatus.OK, "토론 삭제 성공"),
    SEND_DEBATE_LIST_SUCCESS(HttpStatus.OK, "토론 목록 조회 성공"),
    SEND_DEBATE_LIKE_HATE_SUCCESS(HttpStatus.OK, "토론 좋아요/싫어요 처리 성공"),
    SEND_DEBATE_DETAIL_SUCCESS(HttpStatus.OK, "토론 상세 조회 성공"),
    REPORT_LIST_SUCCESS(HttpStatus.OK,"신고 목록 조회 성공"),
    REPORT_UPDATE_SUCCESS(HttpStatus.OK,"신고 처리 성공"),
    ALARM_READ_SUCCESS(HttpStatus.OK, "알람 읽음 처리 성공"),
    SEND_ALARM_LIST_SUCCESS(HttpStatus.OK, "알람 히스토리 목록 반환 성공"),
    SEND_RECOMMENDATION_SUCCESS(HttpStatus.OK,"유사한 성향 리뷰 조회 성공");
    /**
     * 201
     */

    private final HttpStatus httpStatus;
    private final String message;

    public int getStatusCode() {
        return this.httpStatus.value();
    }
}
