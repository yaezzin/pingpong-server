package com.app.pingpong.domain.notification.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    private Long memberId; // 나에게 할일을 미룬 사람의 id
    private Long planId; // 할 일 id
}
