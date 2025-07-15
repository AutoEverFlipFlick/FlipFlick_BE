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
    SEND_CAST_LIST_SUCCESS(HttpStatus.OK,"배우 리스트 조회 성공"),
    SEND_CAST_DETAIL_SUCCESS(HttpStatus.OK,"배우 상세 조회 성공"),

    /**
     * 201
     */

    ;

    private final HttpStatus httpStatus;
    private final String message;

    public int getStatusCode() {
        return this.httpStatus.value();
    }
}