package com.flipflick.backend.api.member.entity;

import com.flipflick.backend.api.follow.entity.Follow;
import com.flipflick.backend.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
    private String socialId;
    private LocalDateTime blockDate;
    private String profileImage;

    @Column(name = "block")
    @Builder.Default
    private Integer block = 0;  // 0 : 정상, 1 : 정지, 2 : 차단

    @Column(name = "warn_count")
    @Builder.Default
    private Integer warnCount = 0;
    
    @Column(name = "popcorn")
    @Builder.Default
    private Double popcorn = 41.0; // 초기 팝콘지수 41.0

    @Column(name = "total_exp")
    @Builder.Default
    private Double totalExp = 0.0; // 누적 경험치

    @Column(name = "block_count")
    @Builder.Default
    private Integer blockCount = 0;

    private Integer isDeleted;
    private LocalDateTime deleteDate;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String refreshToken;
    private LocalDateTime refreshTokenExpiration;

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

    @Builder.Default
    @OneToMany(mappedBy = "followed")
    private List<Follow> followers = new ArrayList<>();

    @OneToMany(mappedBy = "following")
    private List<Follow> followings = new ArrayList<>();

    // 팝콘지수 관련 메서드들 수정
    public void updateTotalExp(Double expToAdd) {
        String previousGrade = getPopcornGrade(this.popcorn);
        this.totalExp = this.totalExp + expToAdd;
        calculatePopcornScore();

        String newGrade = getPopcornGrade(this.popcorn);

        // 등급이 올라간 경우 알림 필요 여부 반환을 위한 플래그 설정
        if (!previousGrade.equals(newGrade) && isGradeUpgrade(previousGrade, newGrade)) {
            // 여기서는 알림 서비스를 직접 호출하지 않고, 서비스 레이어에서 처리하도록 함
        }
    }

    private boolean isGradeUpgrade(String previousGrade, String newGrade) {
        Map<String, Integer> gradeOrder = Map.of(
                "옥수수 1", 1,
                "옥수수 2", 2,
                "옥수수 3", 3,
                "빈 팝콘", 4,
                "1/3 팝콘", 5,
                "2/3 팝콘", 6,
                "1 팝콘", 7,
                "팝콘기계", 8
        );

        return gradeOrder.getOrDefault(newGrade, 0) > gradeOrder.getOrDefault(previousGrade, 0);
    }

    private void calculatePopcornScore() {
        // 로그 기반 팝콘지수 계산 (음수 경험치 허용)
        double baseScore = Math.log(Math.max(1.0, this.totalExp + 1)) / Math.log(1.5);

        // 🎯 수정: 초기값 41.0을 기본으로 하고 경험치에 따라 추가/차감
        double finalScore = 41.0 + baseScore; // 경험치 0일 때 41.0

        // 경험치가 음수인 경우 처리
        if (this.totalExp < 0) {
            // 음수 경험치만큼 초기값에서 차감
            finalScore = 41.0 + (this.totalExp * 0.5); // 음수 경험치 페널티 적용
        }

        // 최종 팝콘지수는 0.0 ~ 100.0 범위
        finalScore = Math.max(0.0, Math.min(finalScore, 100.0));

        this.popcorn = Math.round(finalScore * 10) / 10.0; // 소수점 첫째 자리까지
    }

    //관리가가 경고 시 점수 차감
    private void warnPopcorn(){
        this.popcorn = this.popcorn-5;
    }

    //관리가가 정지 시 다운그레이드
    private void blockPopcorn(){
        // block 상태에 따른  페널티
        if (this.block == 1) { // 정지 상태
            this.popcorn = applyGradeDemotion(this.popcorn); // 한 단계 낮춤
        } else if (this.block == 2) { // 영구 차단
            this.popcorn = 0.0; // 최하위로
        }
    }


    // 한 단계 낮춤 (정지 상태용)
    private double applyGradeDemotion(double currentScore) {
        if (currentScore >= 81) return 71.0;      // 팝콘기 → 1 팝콘
        else if (currentScore >= 71) return 61.0; // 1 팝콘 → 2/3 팝콘
        else if (currentScore >= 61) return 51.0; // 2/3 팝콘 → 1/3 팝콘
        else if (currentScore >= 51) return 41.0; // 1/3 팝콘 → 빈 팝콘
        else if (currentScore >= 41) return 31.0; // 빈 팝콘 → 옥수수 3
        else if (currentScore >= 31) return 21.0; // 옥수수 3 → 옥수수 2
        else if (currentScore >= 21) return 11.0; // 옥수수 2 → 옥수수 1
        else return 0.0; // 옥수수 1 → 최하위
    }

    // 팝콘지수 초기화 (테스트용)
    public void resetPopcornScore() {
        this.popcorn = 41.0;
        this.totalExp = 0.0;
    }
    public String getPopcornGrade(Double popcornScore) {
        if (popcornScore >= 81) return "팝콘기계";
        else if (popcornScore >= 71) return "1 팝콘";
        else if (popcornScore >= 61) return "2/3 팝콘";
        else if (popcornScore >= 51) return "1/3 팝콘";
        else if (popcornScore >= 41) return "빈 팝콘";
        else if (popcornScore >= 31) return "옥수수 3";
        else if (popcornScore >= 21) return "옥수수 2";
        else return "옥수수 1";
    }

    public void addWarning() {
        this.warnCount += 1;
        this.warnPopcorn();
    }

    public void suspend() {
        this.block = 1;
        this.blockCount += 1;
        this.blockDate = LocalDateTime.now();
        this.blockPopcorn();
    }

    public void blockPermanently() {
        this.block = 2;
        this.blockDate = LocalDateTime.now();
        this.blockPopcorn();
    }

    public void unblock() {
        this.block = 0;
        this.blockDate = null;
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

}

