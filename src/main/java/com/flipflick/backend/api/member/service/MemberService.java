package com.flipflick.backend.api.member.service;

import com.flipflick.backend.api.member.dto.*;
import com.flipflick.backend.api.member.entity.Member;
import com.flipflick.backend.api.member.repository.MemberRepository;
import com.flipflick.backend.common.exception.BadRequestException;
import com.flipflick.backend.common.jwt.JWTUtil;
import com.flipflick.backend.common.response.ErrorStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final JWTUtil jwtUtil;

    @Transactional
    public void signup(MemberSignupRequestDto requestDto) {

        // 만약 이미 해당 이메일로 가입된 정보가 있다면 예외처리
        if (memberRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            throw new BadRequestException(ErrorStatus.ALREADY_REGISTERED_ACCOUNT_EXCEPTION.getMessage());
        }

        // 비밀번호랑 비밀번호 재확인 값이 다를 경우 예외처리
        if (!requestDto.getPassword().equals(requestDto.getCheckedPassword())) {
            throw new BadRequestException(ErrorStatus.PASSWORD_MISMATCH_EXCEPTION.getMessage());
        }

        // 패스워드 암호화
        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());
        String imageUrl = requestDto.getProfileImage();

        Member member = requestDto.toEntity(encodedPassword, imageUrl);
        memberRepository.save(member);
    }

    public LoginResponseDto login(LoginRequestDto requestDto) {

        // 1. 이메일로 사용자 조회
        Member member = memberRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new BadRequestException(ErrorStatus.NOT_REGISTER_USER_EXCEPTION.getMessage()));

        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(requestDto.getPassword(), member.getPassword())) {
            throw new BadRequestException(ErrorStatus.INVALID_PASSWORD_EXCEPTION.getMessage());
        }

        // 3. JWT 생성
        String accessToken = jwtUtil.createJwt("access", member.getId(), member.getRole().name(), 1000 * 60 * 30L);
        String refreshToken = jwtUtil.createJwt("refresh", member.getId(), member.getRole().name(), 1000L * 60 * 60 * 24 * 7);

        // 4. Refresh 토큰 저장 (DB에)
        member.updateRefreshToken(refreshToken, 1000L * 60 * 60 * 24 * 7);
        memberRepository.save(member);

        return new LoginResponseDto(accessToken,refreshToken);



    }


    // 닉네임 변경
    @Transactional
    public void updateNickname(String email, String nickname) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException(ErrorStatus.NOT_REGISTER_USER_EXCEPTION.getMessage()));
        member.updateNickname(nickname);
    }

    // 비밀번호 변경
    @Transactional
    public void updatePassword(String email, @Valid PasswordUpdateRequestDto request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException(ErrorStatus.PASSWORD_MISMATCH_EXCEPTION.getMessage());
        }

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException(ErrorStatus.NOT_REGISTER_USER_EXCEPTION.getMessage()));

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        member.updatePassword(encodedPassword);
    }


    public Member getMemberInfo(Long userId) {

        Member member = memberRepository.findById(userId).orElseThrow(()-> new BadRequestException(
                ErrorStatus.NOT_REGISTER_USER_EXCEPTION.getMessage()));
        return member;
    }
}
