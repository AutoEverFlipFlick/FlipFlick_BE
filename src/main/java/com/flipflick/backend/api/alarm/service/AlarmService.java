package com.flipflick.backend.api.alarm.service;

import com.flipflick.backend.api.alarm.dto.AlarmDTO;
import com.flipflick.backend.api.alarm.entity.Alarm;
import com.flipflick.backend.api.alarm.event.AlarmEvent;
import com.flipflick.backend.api.alarm.repository.AlarmRepository;
import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AlarmService {
    private final AlarmRepository repo;
    private final ApplicationEventPublisher publisher;

    public AlarmService(AlarmRepository repo,
                        ApplicationEventPublisher publisher) {
        this.repo = repo;
        this.publisher = publisher;
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
}