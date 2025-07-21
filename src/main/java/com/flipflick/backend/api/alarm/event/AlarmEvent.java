package com.flipflick.backend.api.alarm.event;

import com.flipflick.backend.api.alarm.entity.Alarm;
import org.springframework.context.ApplicationEvent;

public class AlarmEvent extends ApplicationEvent {
    private final Alarm alarm;

    public AlarmEvent(Object source, Alarm alarm) {
        super(source);
        this.alarm = alarm;
    }

    public Alarm getAlarm() {
        return alarm;
    }
}