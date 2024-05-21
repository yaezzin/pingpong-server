package com.app.pingpong.domain.notification.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PushMessage {

    TODO("누군가 할 일을 넘겼어요!", "확인해보러 가요."),
    TEAM("친구 요청이 왔어요!", "누군지 확인해봐요."),
    FRIEND("팀에 초대되었어요!", "확인하러 갈까요?"),
    HOST("누군가 방장을 넘겼어요!", "팀의 방장이 되었어요!");

    String title;
    String body;
}