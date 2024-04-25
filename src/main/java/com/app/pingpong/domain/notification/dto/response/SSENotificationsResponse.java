package com.app.pingpong.domain.notification.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class SSENotificationsResponse {

    private List<SSENotificationResponse> notificationResponses;  // 로그인 한 유저의 모든 알림
    private long unreadCount;                                  // 로그인한 유저가 읽지 않은 알림 수

    @Builder
    public SSENotificationsResponse(List<SSENotificationResponse> notificationResponses, long unreadCount) {
        this.notificationResponses = notificationResponses;
        this.unreadCount = unreadCount;
    }

    public static SSENotificationsResponse of(List<SSENotificationResponse> notificationResponses, long count) {
        return SSENotificationsResponse.builder()
                .notificationResponses(notificationResponses)
                .unreadCount(count)
                .build();
    }
}
