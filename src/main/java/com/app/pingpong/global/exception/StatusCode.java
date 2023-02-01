package com.app.pingpong.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StatusCode {

    SUCCESS(true, 200, "요청에 성공하였습니다."),
    SUCCESS_VALIDATE_NICKNAME(true, 200, "닉네임 형식 확인에 성공하였습니다."),
    SUCCESS_DELETE_USER(true, 200, "유저 탈퇴에 성공하였습니다."),
    SUCCESS_DELETE_AWS_S3(true, 200, "S3에서 사진 삭제를 성공하였습니다."),
    SUCCESS_ACCEPT_FRIEND(true, 200, "친구 신청 수락에 성공하였습니다."),

    /* 400 : Bad Request   */
    INVALID_REFRESH_TOKEN(false, 400, "유효하지 않은 리프레시 토큰입니다."),
    INVALID_SOCIAL_TYPE(false, 400, "소셜로그인 타입이 일치하지 않습니다."),
    INVALID_NICKNAME(false, 400, "닉네임 형식이 올바르지 않습니다."),

    USER_NICKNAME_ALREADY_EXISTS(false, 400, "이미 사용중인 닉네임입니다."),

    USER_ALREADY_FRIEND_REQUEST(false, 400, "해당 유저에게 이미 친구 신청을 하였습니다."),
    USER_ALREADY_GET_FRIEND_REQUEST(false, 400, "나에게 이미 친구 신청을 보낸 유저입니다."),
    ALREADY_ON_FRIEND(false, 400, "이미 친구인 유저입니다."),

    /* 404 : NOT FOUND */
    USER_NOT_FOUND(false, 404, "해당 유저를 찾을 수 없습니다."),
    EMAIL_NOT_FOUND(false, 404, "해당 이메일을 찾을 수 없습니다."),
    FRIEND_NOT_FOUND(false, 404, "해당 유저와는 친구 상태가 아니거나, 친구 신청이 존재하지 않습니다."),

    /* 500 */
    DATABASE_ERROR(false, 500, "데이터베이스 오류입니다.");

    private final boolean isSuccess;
    private final int code;
    private final String message;

}
