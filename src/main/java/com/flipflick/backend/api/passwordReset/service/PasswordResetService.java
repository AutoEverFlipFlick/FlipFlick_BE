package com.flipflick.backend.api.passwordReset.service;

import com.flipflick.backend.api.member.entity.Member;
import com.flipflick.backend.api.member.repository.MemberRepository;
import com.flipflick.backend.api.passwordReset.entity.PasswordReset;
import com.flipflick.backend.api.passwordReset.repository.PasswordResetRepository;
import com.flipflick.backend.common.exception.BadRequestException;
import com.flipflick.backend.common.exception.NotFoundException;
import com.flipflick.backend.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final MemberRepository memberRepository;
    private final PasswordResetRepository passwordResetRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    // 1. 인증 코드 생성 및 이메일 발송
    public void sendResetLink(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.EMAIL_NOT_FOUND.getMessage()));

        String code = UUID.randomUUID().toString().replace("-", "");
        LocalDateTime expiration = LocalDateTime.now().plusMinutes(15);

        PasswordReset reset = PasswordReset.builder()
                .email(email)
                .code(code)
                .expirationTime(expiration)
                .isVerified(false)
                .build();

        passwordResetRepository.save(reset);

        String resetLink = "http://localhost:5173/reset-password-confirm?code=" + code;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[FlipFlick] 비밀번호 재설정 안내");
        message.setText("비밀번호를 재설정하려면 아래 링크를 클릭하세요:\n\n" + resetLink);
        mailSender.send(message);
    }

    // 2. 인증코드 검증 및 비밀번호 재설정
    public void resetPasswordWithCode(String code, String newPassword) {
        PasswordReset reset = passwordResetRepository.findByCode(code)
                .orElseThrow(() -> new BadRequestException(ErrorStatus.PASSWORD_RESET_INVALID_CODE.getMessage()));

        if (reset.getExpirationTime().isBefore(LocalDateTime.now())) {
            throw new BadRequestException(ErrorStatus.PASSWORD_RESET_EXPIRED_CODE.getMessage());
        }

        if (reset.isVerified()) {
            throw new BadRequestException(ErrorStatus.PASSWORD_RESET_ALREADY_USED.getMessage());
        }

        Member member = memberRepository.findByEmail(reset.getEmail())
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOT_FOUND.getMessage()));

        member.changePassword(passwordEncoder.encode(newPassword));
        memberRepository.save(member);

        reset.markVerified(); // 인증코드 사용 처리
        passwordResetRepository.save(reset);
    }
}