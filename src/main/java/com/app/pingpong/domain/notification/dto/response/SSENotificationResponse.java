package com.app.pingpong.domain.notification.dto.response;

import com.app.pingpong.domain.notification.entity.SSENotification;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class SSENotificationResponse {
    private String id;
    private String content;
    private LocalDateTime createdAt;
    private boolean isClicked;

    @Builder
    public SSENotificationResponse(String id, String content, LocalDateTime createdAt, boolean isClicked) {
        this.id = id;
        this.content = content;
        this.createdAt = createdAt;
        this.isClicked = isClicked;
    }

    public static SSENotificationResponse from(SSENotification notification) {
        return SSENotificationResponse.builder()
                .id(notification.getId())
                .content(notification.getContent())
                .createdAt(notification.getCreatedAt())
                .isClicked(notification.isClicked())
                .build();
    }
}
