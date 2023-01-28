package com.app.pingpong.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StatusCode {

    SUCCESS(true, 200, "요청에 성공하였습니다."),

    INVALID_SOCIAL_TYPE(false, 400, "소셜로그인 타입이 ~하지 않습니다.");

    private final boolean isSuccess;
    private final int code;
    private final String message;

}
