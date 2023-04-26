package com.app.pingpong.domain.notification.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTodoResponse {
    private String message;

    public static NotificationTodoResponse of(String nickname, String title) {
        String str = nickname + "님이 할 일 " + title + "을 회원님께 넘겼어요";
        return new NotificationTodoResponse(str);
    }
}
