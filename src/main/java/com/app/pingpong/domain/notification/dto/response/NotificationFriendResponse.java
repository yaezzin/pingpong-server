package com.app.pingpong.domain.notification.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationFriendResponse {
    private String message;

    public static NotificationFriendResponse of(String nickname) {
        String str = nickname + "님이 친구신청을 보냈어요";
        return new NotificationFriendResponse(str);
    }
}
