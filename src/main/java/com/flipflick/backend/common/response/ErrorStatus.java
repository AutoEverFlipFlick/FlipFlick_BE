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


    /**
     * 401 UNAUTHORIZED
     */
    USER_UNAUTHORIZED(HttpStatus.UNAUTHORIZED,"인증되지 않은 사용자입니다."),
    UNAUTHORIZED_REFRESH_TOKEN_EXCEPTION(HttpStatus.UNAUTHORIZED,"유효하지 않은 리프레시 토큰입니다."),
    EXPIRED_TOKEN_EXCEPTION(HttpStatus.UNAUTHORIZED,"만료된 토큰입니다."),
    INVALID_SIGNATURE_EXCEPTION(HttpStatus.UNAUTHORIZED,"비정상적인 서명입니다."),
    MALFORMED_TOKEN_EXCEPTION(HttpStatus.UNAUTHORIZED,"유효하지 않은 토큰입니다."),
    PLAYLIST_ACCESS_DENIED(HttpStatus.FORBIDDEN, "플레이리스트에 접근할 권한이 없습니다."),

    /**
     * 403 FORBIDDEN
     */

    /**
     * 404 NOT_FOUND
     */
    NOT_LOGIN_EXCEPTION(HttpStatus.NOT_FOUND,"로그인이 필요합니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),

    // PlayList 관련 에러
    PLAYLIST_NOT_FOUND(HttpStatus.NOT_FOUND, "플레이리스트를 찾을 수 없습니다."),
    MOVIE_NOT_FOUND(HttpStatus.NOT_FOUND, "영화 정보를 찾을 수 없습니다."),

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