package com.app.pingpong.domain.notification.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTeamRequest {
    private Long memberId; // 내가 초대를 보내는 유저의 Pk
    private Long teamId;
}
