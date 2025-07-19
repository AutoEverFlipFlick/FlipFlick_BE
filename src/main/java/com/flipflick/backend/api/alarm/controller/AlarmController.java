package com.flipflick.backend.api.alarm.controller;

import com.flipflick.backend.api.alarm.dto.AlarmDTO;
import com.flipflick.backend.api.alarm.entity.Alarm;
import com.flipflick.backend.api.alarm.event.AlarmEvent;
import com.flipflick.backend.api.alarm.service.AlarmService;
import com.flipflick.backend.common.exception.BadRequestException;
import com.flipflick.backend.common.response.ErrorStatus;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/api/v1/alarms")
@RequiredArgsConstructor
public class AlarmController {

    private final AlarmService alarmService;

    // userId → 구독자별로 여러 탭/세션을 지원하기 위해 List<SseEmitter> 사용
    private final Map<Long, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();

    //SSE 구독 @param userId 로그인된 사용자의 ID
    @CrossOrigin(
            origins = "http://localhost:5173" )
    @Operation(summary = "알람 스트림 구독 API", description = "로그인된 사용자의 SSE 알람 스트림을 구독합니다.")
    @GetMapping("/stream")
    public SseEmitter subscribe(@RequestParam Long userId) {
        if (userId == null || userId < 0) {
            throw new BadRequestException(ErrorStatus.NOT_REGISTER_USER_EXCEPTION.getMessage());
        }
        // 60분 타임아웃
        SseEmitter emitter = new SseEmitter(60 * 60 * 1000L);

        // 중복 연결 방지: 기존 emitter 닫기
        List<SseEmitter> list = emitters.computeIfAbsent(userId, id -> new CopyOnWriteArrayList<>());
        list.forEach(SseEmitter::complete); // 기존 emitter들 닫기
        list.clear(); // 다 지우고
        list.add(emitter); // 새 emitter만 등록

//        emitters
//                .computeIfAbsent(userId, id -> new CopyOnWriteArrayList<>())
//                .add(emitter); // 여러 emitter 허용

        emitter.onError((ex) -> {
            emitters.get(userId).remove(emitter);
        });

        // 구독 직후 아직 읽지 않은 알람을 전송
        List<AlarmDTO> unread = alarmService.getAlarms(userId).stream()
                .filter(a -> !a.getIsRead())
                .toList();

        unread.forEach(a -> safeSend(emitter, a));

        // 완료/타임아웃/오류 시 콜백에서 제거
        emitter.onCompletion(() -> emitters.get(userId).remove(emitter));
        emitter.onTimeout   (() -> emitters.get(userId).remove(emitter));
        emitter.onError     ((ex) -> emitters.get(userId).remove(emitter));

        return emitter;
    }

    // AlarmEvent 발생 시, 해당 유저의 모든 emitter 에 푸시
    @EventListener
    public void onAlarmEvent(AlarmEvent event) {
        Alarm alarm = event.getAlarm();
        Long toUser = alarm.getReceivedId();

        AlarmDTO dto = AlarmDTO.from(alarm); // ✅ DTO 변환

        List<SseEmitter> userEmitters = emitters.get(toUser);
        if (userEmitters != null) {
            userEmitters.forEach(em -> safeSend(em, dto)); // ✅ DTO를 안전하게 전송
        }
    }


    // 과거 알람 전체 조회
    @Operation(summary = "알람 히스토리 조회", description = "사용자의 과거 알람을 최신순으로 조회합니다.")
    @GetMapping
    public List<AlarmDTO> getHistory(@RequestParam Long userId) {
        return alarmService.getAlarms(userId);
    }

    // 특정 알람 읽음 처리
    @Operation(summary = "알람 읽음 처리", description = "특정 알람을 읽음 처리 후 삭제합니다.")
    @PostMapping("/{alarmId}/read")
    public ResponseEntity<Void> markRead(@PathVariable Long alarmId) {
        alarmService.markRead(alarmId);
        return ResponseEntity.ok().build();
    }

    // 안전하게 SSE 이벤트를 보내는 헬퍼
    private void safeSend(SseEmitter emitter, AlarmDTO alarm) {
        try {
            emitter.send(SseEmitter.event().name("alarm").data(alarm));
        } catch (IOException e) {
            emitters.forEach((userId, emitterList) -> emitterList.remove(emitter));
        }
    }
}