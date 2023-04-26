package com.app.pingpong.domain.notification.dto.response;

import com.app.pingpong.domain.member.entity.Member;
import com.app.pingpong.domain.notification.entity.Notification;
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

    public static NotificationResponse of(Notification notification, Member member) {
        return new NotificationResponse(notification.getType(), member.getProfileImage(), notification.getMessage(), notification.getIsClicked(), notification.getIsAccepted());
    }

}
