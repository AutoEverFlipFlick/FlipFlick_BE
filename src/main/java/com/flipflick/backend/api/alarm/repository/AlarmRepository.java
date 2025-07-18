package com.flipflick.backend.api.alarm.repository;

import com.flipflick.backend.api.alarm.entity.Alarm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlarmRepository extends JpaRepository<Alarm, Long> {
    List<Alarm> findByReceivedIdOrderByCreatedAtDesc(Long userId);
}