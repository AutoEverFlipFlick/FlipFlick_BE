package com.flipflick.backend.api.member.service;

import com.flipflick.backend.api.member.dto.*;
import com.flipflick.backend.api.member.entity.Member;
import com.flipflick.backend.api.member.entity.Role;
import com.flipflick.backend.api.member.repository.MemberRepository;
import com.flipflick.backend.common.exception.BaseException;
import com.flipflick.backend.common.jwt.JWTUtil;
import com.flipflick.backend.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KakaoAuthService {
    private final MemberRepository memberRepository;
    private final JWTUtil jwtUtil;

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    public String getAccessToken(String code) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);  // yml에 설정
        params.add("redirect_uri", redirectUri); // 프론트와 동일하게 설정
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<KakaoTokenResponseDto> response = restTemplate.postForEntity(
                "https://kauth.kakao.com/oauth/token", request, KakaoTokenResponseDto.class);


        return response.getBody().getAccessToken();
    }
    public KakaoUserInfo getUserInfo(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<KakaoUserInfo> response = restTemplate.exchange(
                "https://kapi.kakao.com/v2/user/me", HttpMethod.GET, request, KakaoUserInfo.class);


        return response.getBody();
    }

    public Member getOrRegisterUser(KakaoUserInfo userInfo) {
        String kakaoId = userInfo.getId(); // Long 타입

        // 1. 소셜 ID로 유저 찾기
        Optional<Member> memberOpt = memberRepository.findBySocialId(kakaoId);

        if (memberOpt.isPresent()) {
            return memberOpt.get();
        }

        // 2. 랜덤 이메일 생성 (이건 단지 형식용)
        String randomEmail = "kakao-" + kakaoId + "@kakao.com";

        // 3. 새로운 유저 등록
        Member newMember = Member.builder()
                .email(randomEmail)
                .nickname(null)
                .profileImage(null)
                .socialType("KAKAO")
                .socialId(kakaoId)
                .block(0)
                .isDeleted(0)
                .role(Role.ROLE_USER)
                .build();

        return memberRepository.save(newMember);
    }
    public OauthLoginResponseDto kakaoLogin(String code) {
        String accessToken = getAccessToken(code);
        KakaoUserInfo userInfo = getUserInfo(accessToken);

        Member member = getOrRegisterUser(userInfo);


        if (member.getBlock() == 2) {
            throw new BaseException(ErrorStatus.MEMBER_BLOCKED.getHttpStatus(), ErrorStatus.MEMBER_BLOCKED.getMessage());
        }

        if (member.getBlock() == 1) {
            LocalDateTime unblockDate = member.getBlockDate().plusDays(7);
            if (member.getBlockDate().plusDays(7).isBefore(LocalDateTime.now())) {
                member.unblock();
            } else {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                String formattedDate = unblockDate.format(formatter);
                throw new BaseException(
                        ErrorStatus.MEMBER_SUSPENDED.getHttpStatus(),
                        "정지된 회원입니다. 해제일: " + formattedDate
                );
            }
        }

        String jwtAccessToken = jwtUtil.createJwt("access", member.getId(), member.getRole().name(), 1000 * 60 * 30L);
        String jwtRefreshToken = jwtUtil.createJwt("refresh", member.getId(), member.getRole().name(), 1000L * 60 * 60 * 24 * 7);

        member.updateRefreshToken(jwtRefreshToken, 1000L * 60 * 60 * 24 * 7);
        memberRepository.save(member);

        boolean isNew = (member.getNickname() == null);


        return new OauthLoginResponseDto(jwtAccessToken, jwtRefreshToken, isNew);
    }


}
