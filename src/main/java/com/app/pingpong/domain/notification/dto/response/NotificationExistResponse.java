package com.app.pingpong.domain.notification.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationExistResponse {
    private boolean notificationExists;

    public static NotificationExistResponse of(boolean status) {
        return new NotificationExistResponse(status);
    }
}
