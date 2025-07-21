package com.flipflick.backend.common.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)

public enum ErrorStatus {

    /**
     * 400 BAD_REQUEST
     */
    VALIDATION_REQUEST_MISSING_EXCEPTION(HttpStatus.BAD_REQUEST, "요청 값이 입력되지 않았습니다."),
    ALREADY_REGISTERED_ACCOUNT_EXCEPTION(HttpStatus.BAD_REQUEST, "이미 회원가입된 이메일입니다."),
    NOT_MATCHED_LOGIN_USER_EXCEPTION(HttpStatus.BAD_REQUEST, "아이디 또는 비밀번호가 일치하지 않습니다."),
    NOT_REGISTER_USER_EXCEPTION(HttpStatus.BAD_REQUEST, "존재하지 않는 사용자 입니다."),
    PLAYLIST_CREATION_FAILED(HttpStatus.BAD_REQUEST, "플레이리스트 생성에 실패했습니다."),
    PLAYLIST_BOOKMARK_FAILED(HttpStatus.BAD_REQUEST, "플레이리스트 북마크 처리에 실패했습니다."),
    SELF_BOOKMARK_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "본인이 만든 플레이리스트는 북마크할 수 없습니다."),
    PASSWORD_MISMATCH_EXCEPTION(HttpStatus.BAD_REQUEST, "비밀번호와 비밀번호 확인 값이 일치하지 않습니다."),
    INVALID_PASSWORD_EXCEPTION(HttpStatus.BAD_REQUEST, "비밀번호가 올바르지 않습니다."),
    NOT_RELEASE_MOVIE_EXCEPTION(HttpStatus.BAD_REQUEST,"아직 개봉되지 않은 영화 입니다."),
    NOT_REGISTER_MOVIE_EXCEPTION(HttpStatus.BAD_REQUEST, "존재하지 않는 영화 입니다."),
    NOT_REGISTER_CAST_EXCEPTION(HttpStatus.BAD_REQUEST,"존재하지 않는 배우 입니다."),
    EMPTY_FILE(HttpStatus.BAD_REQUEST, "업로드할 파일이 비어있습니다."),
    REVIEW_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 해당 영화에 리뷰를 작성하셨습니다."),
    REVIEW_INVALID_STAR_RATING(HttpStatus.BAD_REQUEST, "별점은 1.0~5.0 사이의 0.5 단위로 입력해주세요."),
    REVIEW_UPDATE_DENIED(HttpStatus.BAD_REQUEST, "본인이 작성한 리뷰만 수정할 수 있습니다."),
    REVIEW_DELETE_DENIED(HttpStatus.BAD_REQUEST, "본인이 작성한 리뷰만 삭제할 수 있습니다."),
    REVIEW_SELF_LIKE_HATE_DENIED(HttpStatus.BAD_REQUEST, "본인이 작성한 리뷰에는 좋아요/싫어요를 할 수 없습니다."),
    INVALID_IMAGE_URL(HttpStatus.BAD_REQUEST, "유효하지 않은 이미지 URL입니다."),
    FOLLOW_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 팔로우 중입니다."),
    SELF_FOLLOW_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "자신은 팔로우할 수 없습니다."),
    INCORRECT_USER_EXCEPTION(HttpStatus.BAD_REQUEST,"올바르지 않은 회원입니다."),
    INVALID_STATUS(HttpStatus.BAD_REQUEST,"유효하지 않은 상태입니다."),
    ALREADY_REPORT(HttpStatus.BAD_REQUEST,"이미 처리된 신고 입니다."),
    INVALID_REPORT_ACTION(HttpStatus.BAD_REQUEST,"유효하지 않은 신고 처리 요청 입니다."),
    DEBATE_UPDATE_DENIED(HttpStatus.BAD_REQUEST, "본인이 작성한 토론만 수정할 수 있습니다."),
    DEBATE_DELETE_DENIED(HttpStatus.BAD_REQUEST, "본인이 작성한 토론만 삭제할 수 있습니다."),
    DEBATE_SELF_LIKE_HATE_DENIED(HttpStatus.BAD_REQUEST, "본인이 작성한 토론에는 좋아요/싫어요를 할 수 없습니다."),
    DEBATE_COMMENT_UPDATE_DENIED(HttpStatus.BAD_REQUEST, "본인이 작성한 댓글만 수정할 수 있습니다."),
    DEBATE_COMMENT_DELETE_DENIED(HttpStatus.BAD_REQUEST, "본인이 작성한 댓글만 삭제할 수 있습니다."),

    /**
     * 401 UNAUTHORIZED
     */
    USER_UNAUTHORIZED(HttpStatus.UNAUTHORIZED,"인증되지 않은 사용자입니다."),
    UNAUTHORIZED_REFRESH_TOKEN_EXCEPTION(HttpStatus.UNAUTHORIZED,"유효하지 않은 리프레시 토큰입니다."),
    EXPIRED_TOKEN_EXCEPTION(HttpStatus.UNAUTHORIZED,"만료된 토큰입니다."),
    INVALID_SIGNATURE_EXCEPTION(HttpStatus.UNAUTHORIZED,"비정상적인 서명입니다."),
    MALFORMED_TOKEN_EXCEPTION(HttpStatus.UNAUTHORIZED,"유효하지 않은 토큰입니다."),
    PLAYLIST_ACCESS_DENIED(HttpStatus.FORBIDDEN, "플레이리스트에 접근할 권한이 없습니다."),
    MALFORMED_ACCESS_TOKEN_EXCEPTION(HttpStatus.UNAUTHORIZED,"유효하지 않은 Access 토큰입니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED,  "Refresh Token이 존재하지 않습니다."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "Refresh Token이 만료되었습니다."),

    /**
     * 403 FORBIDDEN
     */
    MEMBER_BLOCKED(HttpStatus.FORBIDDEN, "차단된 회원입니다."),
    MEMBER_SUSPENDED(HttpStatus.FORBIDDEN, "정지된 회원입니다."),

    /**
     * 404 NOT_FOUND
     */
    NOT_LOGIN_EXCEPTION(HttpStatus.NOT_FOUND,"로그인이 필요합니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "신고를 찾을 수 없습니다."),

    // PlayList 관련 에러
    PLAYLIST_NOT_FOUND(HttpStatus.NOT_FOUND, "플레이리스트를 찾을 수 없습니다."),
    MOVIE_NOT_FOUND(HttpStatus.NOT_FOUND, "영화 정보를 찾을 수 없습니다."),

    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다."),
    DEBATE_NOT_FOUND(HttpStatus.NOT_FOUND, "토론을 찾을 수 없습니다."),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다."),

    // follow 관련 에러
    FOLLOW_NOT_FOUND(HttpStatus.NOT_FOUND, "팔로우 정보가 없습니다."),
    FOLLOW_USER_INFO_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자 정보가 없습니다."),
    TARGET_USER_NOT_FOUND(HttpStatus.NOT_FOUND, "대상 회원을 찾을 수 없습니다."),
  
    /**
     * 500 SERVER_ERROR
     */
    NO_RESPONSE_TMDB_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "데이터 조회 중 에러가 발생하였습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String message;

    public int getStatusCode() {
        return this.httpStatus.value();
    }
}