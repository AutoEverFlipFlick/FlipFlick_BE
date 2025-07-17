package com.flipflick.backend.api.member.dto;

import lombok.Getter;

@Getter
public class KakaoUserInfo {
    private Long id;
    private KakaoAccount kakao_account;
    private Properties properties;

    @Getter
    public static class KakaoAccount {
        private Profile profile;

        @Getter
        public static class Profile {
            private String nickname;
            private boolean is_default_nickname;
        }
    }

    @Getter
    public static class Properties {
        private String nickname;
    }
}