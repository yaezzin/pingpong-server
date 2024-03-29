package com.app.pingpong.domain.notification.dto.response;

import com.app.pingpong.domain.member.entity.Member;
import com.app.pingpong.domain.notification.entity.Notification;
import com.app.pingpong.global.common.status.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class NotificationResponse {
    private String notificationId;
    private Status type;
    private Long memberId;
    private String profileImage;
    private Long teamId;
    private String message;
    private Boolean isClicked;
    private Boolean isAccepted;
    private String createdAt;


    public static NotificationResponse of(Notification notification, Member member) {
        String days = null;

        if (notification.getCreatedAt() != null) {
            Duration duration = Duration.between(notification.getCreatedAt(), LocalDateTime.now());
            long day = duration.toDays();

            if (day != 0) {
                days = day + "일 전";
            } else {
                days = "오늘";
            }
        }

        return new NotificationResponse(notification.getId(), notification.getType(), member.getId(), member.getProfileImage(),
                notification.getTeamId(), notification.getMessage(), notification.getIsClicked(), notification.getIsAccepted(), days);
    }
}
