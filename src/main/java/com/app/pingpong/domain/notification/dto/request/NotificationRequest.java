package com.app.pingpong.domain.notification.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    private Long memberId; // 내가 할 일을 미루려고 하는 사람의 id
    private Long planId; // 할 일 id
}
