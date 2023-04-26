package com.app.pingpong.domain.notification.dto.response;

import com.app.pingpong.global.common.status.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class NotificationResponse {
    private Status type;
    private String profileImage;
    private String message;
    private Boolean isClicked;
    private Boolean isAccepted;
}
