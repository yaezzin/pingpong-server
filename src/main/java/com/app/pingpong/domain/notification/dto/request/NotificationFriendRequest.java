package com.app.pingpong.domain.notification.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationFriendRequest {
    private Long memberId; // 내가 친구 신청을 한 유저의 id
}
