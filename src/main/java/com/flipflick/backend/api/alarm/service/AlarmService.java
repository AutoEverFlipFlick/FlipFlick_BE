package com.flipflick.backend.api.alarm.service;

import com.flipflick.backend.api.alarm.dto.AlarmDTO;
import com.flipflick.backend.api.alarm.entity.Alarm;
import com.flipflick.backend.api.alarm.event.AlarmEvent;
import com.flipflick.backend.api.alarm.repository.AlarmRepository;
import com.flipflick.backend.api.follow.entity.Follow;
import com.flipflick.backend.api.follow.repository.FollowRepository;
import com.flipflick.backend.api.member.entity.Member;
import com.flipflick.backend.api.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class AlarmService {
    private final AlarmRepository repo;
    private final ApplicationEventPublisher publisher;
    private final FollowRepository followRepository;
    private final MemberRepository memberRepository;

    public AlarmService(AlarmRepository repo,
                        ApplicationEventPublisher publisher, FollowRepository followRepository, MemberRepository memberRepository) {
        this.repo = repo;
        this.publisher = publisher;
        this.followRepository = followRepository;
        this.memberRepository = memberRepository;
    }

    /** 알람 생성 + 이벤트 발행 */
    public Alarm createAlarm(Long toUserId, String content) {
        Alarm alarm = new Alarm();
        alarm.setContent(content);
        alarm.setReceivedId(toUserId);
        alarm.setCreatedAt(LocalDateTime.now());
        Alarm saved = repo.save(alarm);

        publisher.publishEvent(new AlarmEvent(this, saved));
        return saved;
    }

    /** 유저별 알람 조회 (최신순) */
    @Transactional
    public List<AlarmDTO> getAlarms(Long userId) {
        return repo.findByReceivedIdOrderByCreatedAtDesc(userId).stream()
                .map(AlarmDTO::from)
                .toList();
    }

    /** 읽음 처리 */
    @Transactional
    public void markRead(Long alarmId) {
        repo.deleteById(alarmId);
    }

    /**
     *  팔로워들에게 일괄 알림 전송
     */
    @Transactional
    public void createAlarmForFollowers(Long writerId, String message) {
        try {
            // 1. 작성자 존재 확인
            Member writer = memberRepository.findById(writerId).orElse(null);
            if (writer == null) {
                log.warn("알림 생성 실패: 작성자를 찾을 수 없음 - ID: {}", writerId);
                return;
            }

            // 2. 작성자를 팔로우하는 사용자들 조회
            List<Member> followers = followRepository.findAllByFollowed(writer)
                    .stream()
                    .map(Follow::getFollowing) // 팔로워들
                    .toList();

            if (followers.isEmpty()) {
                log.debug("팔로워가 없어 알림을 보내지 않음 - 작성자: {}", writer.getNickname());
                return;
            }

            // 3. 각 팔로워에게 알림 생성
            int successCount = 0;
            for (Member follower : followers) {
                try {
                    createAlarm(follower.getId(),message);
                    successCount++;

                    log.debug("팔로워 알림 생성 성공: {} -> {}", writer.getNickname(), follower.getNickname());
                } catch (Exception e) {
                    log.error("개별 팔로워 알림 생성 실패: {} -> {}", writer.getNickname(), follower.getNickname(), e);
                }
            }

            log.info("팔로워 대상 알림 생성 완료 - 작성자: {}, 성공: {}/{}",
                    writer.getNickname(), successCount, followers.size());

        } catch (Exception e) {
            log.error("팔로워 대상 알림 생성 중 예외 발생 - 작성자 ID: {}", writerId, e);
        }
    }

    /**
     *  리뷰 작성 알림
     */
    @Transactional
    public void createReviewWriteAlarmForFollowers(Long writerId, String movieTitle) {
        Member writer = memberRepository.findById(writerId).orElse(null);
        if (writer == null) return;

        String message = String.format("%s님이 '%s' 리뷰를 작성하셨습니다", writer.getNickname(), movieTitle);
        createAlarmForFollowers(writerId, message);
    }

    /**
     *  토론 작성 알림
     */
    @Transactional
    public void createDebateWriteAlarmForFollowers(Long writerId, String debateTitle) {
        Member writer = memberRepository.findById(writerId).orElse(null);
        if (writer == null) return;

        String message = String.format("%s님이 토론 '%s'을 작성하셨습니다", writer.getNickname(), debateTitle);
        createAlarmForFollowers(writerId, message);
    }

    /**
     *  플레이리스트 생성 알림
     */
    @Transactional
    public void createPlaylistCreateAlarmForFollowers(Long writerId, String playlistTitle) {
        Member writer = memberRepository.findById(writerId).orElse(null);
        if (writer == null) return;

        String message = String.format("%s님이 플레이리스트 '%s'를 생성하셨습니다", writer.getNickname(), playlistTitle);
        createAlarmForFollowers(writerId, message);
    }
}