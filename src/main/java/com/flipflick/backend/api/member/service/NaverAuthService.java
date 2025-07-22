package com.flipflick.backend.api.member.service;

import com.flipflick.backend.api.member.dto.*;
import com.flipflick.backend.api.member.entity.Member;
import com.flipflick.backend.api.member.entity.Role;
import com.flipflick.backend.api.member.repository.MemberRepository;
import com.flipflick.backend.common.jwt.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NaverAuthService {

    private final MemberRepository memberRepository;
    private final JWTUtil jwtUtil;

    @Value("${naver.client-id}")
    private String clientId;

    @Value("${naver.client-secret}")
    private String clientSecret;

    @Value("${naver.redirect-uri}")
    private String redirectUri;

    public String getAccessToken(String code, String state) {
        RestTemplate restTemplate = new RestTemplate();

        String tokenUrl = UriComponentsBuilder.fromHttpUrl("https://nid.naver.com/oauth2.0/token")
                .queryParam("grant_type", "authorization_code")
                .queryParam("client_id", clientId)
                .queryParam("client_secret", clientSecret)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("code", code)
                .queryParam("state", state)
                .toUriString();

        ResponseEntity<NaverTokenResponseDto> response = restTemplate.getForEntity(tokenUrl, NaverTokenResponseDto.class);
        return response.getBody().getAccessToken();
    }

    public NaverUserInfo getUserInfo(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        System.out.println(accessToken);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> response1 = restTemplate.exchange(
                "https://openapi.naver.com/v1/nid/me", HttpMethod.GET, request, String.class
        );

        System.out.println("응답 전체: " + response1.getBody());

        ResponseEntity<NaverUserResponse> response = restTemplate.exchange(
                "https://openapi.naver.com/v1/nid/me", HttpMethod.GET, request, NaverUserResponse.class
        );

        System.out.println("응답 전체: " + response1.getBody());


        return response.getBody().getResponse();
    }

    public Member getOrRegisterUser(NaverUserInfo userInfo) {
        Optional<Member> memberOpt = memberRepository.findBySocialIdAndIsDeletedFalse(userInfo.getId());
        if (memberOpt.isPresent()) return memberOpt.get();

        String randomEmail = "naver-" + userInfo.getId() + "@naveer.com";

        Member member = Member.builder()
                .email(randomEmail)
                .nickname(null)
                .socialType("NAVER")
                .block(0)
                .isDeleted(false)
                .socialId(userInfo.getId())
                .role(Role.ROLE_USER)
                .build();


        return memberRepository.save(member);
    }

    public OauthLoginResponseDto naverLogin(String code, String state) {
        String accessToken = getAccessToken(code, state);
        NaverUserInfo userInfo = getUserInfo(accessToken);
        Member member = getOrRegisterUser(userInfo);

        String jwtAccessToken = jwtUtil.createJwt("access", member.getId(), member.getRole().name(), 1000 * 60 * 30L);
        String jwtRefreshToken = jwtUtil.createJwt("refresh", member.getId(), member.getRole().name(), 1000L * 60 * 60 * 24 * 7);

        member.updateRefreshToken(jwtRefreshToken, 1000L * 60 * 60 * 24 * 7);
        memberRepository.save(member);
        boolean isNew = (member.getNickname() == null);


        return new OauthLoginResponseDto(jwtAccessToken, jwtRefreshToken,isNew);
    }
}