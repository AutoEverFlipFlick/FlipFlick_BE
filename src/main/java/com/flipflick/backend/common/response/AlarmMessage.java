package com.flipflick.backend.common.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum AlarmMessage {
    WARN_ALARM("운영 정책 위반으로 경고가 부여되었습니다. 내용 확인 후 유의해 주세요."),
    BLOCK_ALARM("중대한 운영 정책 위반으로 계정이 영구 차단되었습니다. 서비스 이용이 불가능합니다."),
    SUSPEND_ALARM("운영 정책 위반으로 계정이 일시 정지되었습니다."),
    UNBLOCK("계정에 대한 정지 또는 차단 조치가 해제되었습니다. 안전한 서비스 이용을 부탁드립니다.")

    ;

    private final String message;
}
