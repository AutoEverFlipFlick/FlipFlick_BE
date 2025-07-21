package com.flipflick.backend.api.alarm.dto;

import com.flipflick.backend.api.alarm.entity.Alarm;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AlarmDTO {
    private Long id;
    private String content;
    private Boolean isRead;
    private LocalDateTime createdAt;

    public static AlarmDTO from(Alarm alarm) {
        AlarmDTO dto = new AlarmDTO();
        dto.setId(alarm.getId());
        dto.setContent(alarm.getContent());
        dto.setIsRead(alarm.getIsRead());
        dto.setCreatedAt(alarm.getCreatedAt());
        return dto;
    }
}
