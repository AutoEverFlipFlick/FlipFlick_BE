package com.flipflick.backend.api.member.entity;

import com.flipflick.backend.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "member")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    private String email;
    private String nickname;
    private String password;
    private String socialType;
    private Long socialId;
    private Integer block;    // 0 : 정상, 1 : 정지, 2 : 차단
    private LocalDateTime blockDate;
    private String profileImage;
    private Double popcorn;
    private Integer isDeleted;
    private LocalDateTime deleteDate;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String refreshToken;
    private LocalDateTime  refreshTokenExpiration;

    public void updateRefreshToken(String refreshToken, long expireMs) {
        this.refreshToken = refreshToken;
        this.refreshTokenExpiration = LocalDateTime.now().plus(Duration.ofMillis(expireMs));
    }

    //닉네임 변경
    public void updateNickname(String nickname) { this.nickname = nickname; }

    // 비밀번호 변경
    public void updatePassword(String password) { this.password = password; }

    // 프로필 이미지 변경
    public void updateProfileImage(String profileImage) { this.profileImage = profileImage;}
}

