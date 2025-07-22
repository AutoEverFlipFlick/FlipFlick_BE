package com.flipflick.backend.api.member.service;

import com.flipflick.backend.api.aws.s3.service.S3Service;
import com.flipflick.backend.api.member.dto.*;
import com.flipflick.backend.api.member.entity.Member;
import com.flipflick.backend.api.member.repository.MemberRepository;
import com.flipflick.backend.common.exception.BadRequestException;
import com.flipflick.backend.common.exception.BaseException;
import com.flipflick.backend.common.exception.NotFoundException;
import com.flipflick.backend.common.exception.UnauthorizedException;
import com.flipflick.backend.common.jwt.JWTUtil;
import com.flipflick.backend.common.response.ErrorStatus;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final JWTUtil jwtUtil;
    private final S3Service s3Service;

    @Transactional
    public void signup(MemberSignupRequestDto requestDto) {

        // 만약 이미 해당 이메일로 가입된 정보가 있다면 예외처리
        if (memberRepository.findByEmailAndIsDeletedFalse(requestDto.getEmail()).isPresent()) {
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
        Member member = memberRepository.findByEmailAndIsDeletedFalse(requestDto.getEmail())
                .orElseThrow(() -> new BadRequestException(ErrorStatus.NOT_REGISTER_USER_EXCEPTION.getMessage()));

        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(requestDto.getPassword(), member.getPassword())) {
            throw new BadRequestException(ErrorStatus.INVALID_PASSWORD_EXCEPTION.getMessage());
        }

        // 2.5. 정지/차단 상태 확인
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
        Member member = memberRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new BadRequestException(ErrorStatus.NOT_REGISTER_USER_EXCEPTION.getMessage()));
        member.updateNickname(nickname);
    }

    // 비밀번호 변경
    @Transactional
    public void updatePassword(String email, @Valid PasswordUpdateRequestDto request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException(ErrorStatus.PASSWORD_MISMATCH_EXCEPTION.getMessage());
        }

        Member member = memberRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new BadRequestException(ErrorStatus.NOT_REGISTER_USER_EXCEPTION.getMessage()));

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        member.updatePassword(encodedPassword);
    }

    // 프로필 이미지 변경
    @Transactional
    public String updateProfileImage(String email, MultipartFile file) {
        Member member = memberRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new BadRequestException(ErrorStatus.NOT_REGISTER_USER_EXCEPTION.getMessage()));

        try {
            String imageUrl = s3Service.upload(file, "profile");
            member.updateProfileImage(imageUrl);

            return imageUrl;
        } catch (IOException e) {
            throw new RuntimeException("이미지 업로드 실패", e);
        }
    }


    // 회원 정보 조회
    public Member getMemberInfo(Long userId) {

        Member member = memberRepository.findByIdAndIsDeletedFalse(userId).orElseThrow(()-> new BadRequestException(
                ErrorStatus.NOT_REGISTER_USER_EXCEPTION.getMessage()));
        return member;
    }

    // ID로 회원 조회
    public MemberResponseDto getMemberById(Long id) {
        Member member = memberRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원을 찾을 수 없습니다."));
        return MemberResponseDto.of(member);
    }

    @Transactional
    public LoginResponseDto reissueToken(String refresh) {


        if (refresh == null) {
            throw new UnauthorizedException(ErrorStatus.REFRESH_TOKEN_NOT_FOUND.getMessage());
        }

        try {
            jwtUtil.isExpired(refresh);
        } catch (ExpiredJwtException e) {
            throw new UnauthorizedException(ErrorStatus.REFRESH_TOKEN_EXPIRED.getMessage());
        }

        String category = jwtUtil.getCategory(refresh);
        if (!"refresh".equals(category)) {
            throw new UnauthorizedException(ErrorStatus.MALFORMED_TOKEN_EXCEPTION.getMessage());
        }



        Long id = jwtUtil.getId(refresh);
        String role = jwtUtil.getRole(refresh);

        Member member = memberRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOT_FOUND.getMessage()));

        if (!refresh.equals(member.getRefreshToken())) {
            throw new UnauthorizedException(ErrorStatus.MALFORMED_TOKEN_EXCEPTION.getMessage()); // 다른 refresh token인 경우
        }

        // 새 토큰 발급
        String newAccessToken = jwtUtil.createJwt("access", id, role, 1000 * 60 * 30L); // 30분
        String newRefreshToken = jwtUtil.createJwt("refresh", id, role, 1000L * 60 * 60 * 24 * 7); // 7일

        member.updateRefreshToken(newRefreshToken,1000L * 60 * 60 * 24 * 7);
        memberRepository.save(member);

        // 쿠키에 새 refresh 토큰 저장

        return new LoginResponseDto(newAccessToken, newRefreshToken);
    }

    public boolean isEmailDuplicate(String email) {
        return memberRepository.existsByEmailAndIsDeletedFalse(email);
    }

    public boolean isNicknameDuplicate(String nickname) {
        return memberRepository.existsByNicknameAndIsDeletedFalse(nickname);
    }

    @Transactional
    public void updateSocialInfo(Long memberId, SocialInfoRequestDto dto) {
        Member member = memberRepository.findByIdAndIsDeletedFalse(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOT_FOUND.getMessage()));

        member.updateNickname(dto.getNickname());
        member.updateProfileImage(dto.getProfileImage());

    }

    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null) {
            throw new UnauthorizedException(ErrorStatus.REFRESH_TOKEN_NOT_FOUND.getMessage());
        }

        // 토큰에서 사용자 ID 추출
        Long memberId = jwtUtil.getId(refreshToken);

        // 사용자 조회
        Member member = memberRepository.findByIdAndIsDeletedFalse(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOT_FOUND.getMessage()));



        // 리프레시 토큰 삭제
        member.updateRefreshToken(null, 0L);
    }

    @Transactional
    public void softDelete(Long userId) {
        Member member = memberRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOT_FOUND.getMessage()));
        member.softDelete();
        memberRepository.save(member);
    }
}
