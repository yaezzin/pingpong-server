package com.app.pingpong.global.common.status;

public enum Status {
    ACTIVE, // 1. 생성시
    INACTIVE,
    DELETE,
    PERMANENT,
    WAIT, // 3. 친구 대기 시
    COMPLETE,
    INCOMPLETE,

    TODO,
    FRIEND,
    TEAM
}
