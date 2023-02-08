package com.app.pingpong.global.common;

public enum Status {
    ACTIVE, // 1. 생성시
    DELETE,
    WAIT, // 3. 친구 대기 시
    COMPLETE, // 4. 할일 완료 시 (완료전에는 ACTIVE)
    PERMANENT // 5. 휴지통에서 완전 삭제 시
}
