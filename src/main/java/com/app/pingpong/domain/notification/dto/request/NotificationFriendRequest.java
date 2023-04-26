package com.app.pingpong.domain.notification.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationFriendRequest {
    private Long memberId; // 나에게 친구를 신청한 사람의 id
}
