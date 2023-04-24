package com.app.pingpong.global.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StatusCode {

    SUCCESS(true, 200, "요청에 성공하였습니다."),
    SUCCESS_VALIDATE_NICKNAME(true, 200, "닉네임 형식 확인에 성공하였습니다."),
    SUCCESS_DELETE_MEMBER(true, 200, "유저 탈퇴에 성공하였습니다."),
    SUCCESS_DELETE_AWS_S3(true, 200, "S3에서 사진 삭제를 성공하였습니다."),
    SUCCESS_ACCEPT_FRIEND(true, 200, "친구 신청 수락에 성공하였습니다."),
    SUCCESS_REFUSE_FRIEND(true, 200, "친구 신청 거절에 성공하였습니다."),
    SUCCESS_LOGOUT(true, 200, "로그아웃에 성공하였습니다."),
    SUCCESS_ACCEPT_TEAM_INVITATION(true, 200, "그룹 초대 수락에 성공하였습니다."),
    SUCCESS_REFUSE_TEAM_INVITATION(true, 200, "그룹 초대 거절에 성공하였습니다."),
    SUCCESS_DELETE_ALL_TRASH(true, 200, "해당 그룹의 휴지통 전체 삭제에 성공하였습니다."),
    SUCCESS_DELETE_TRASH(true, 200, "해당 그룹의 휴지통에서 할 일(1개) 삭제에 성공하였습니다."),
    SUCCESS_RECOVER_TRASH(true, 200, "해당 할 일을 휴지통에서 복구하였습니다."),
    SUCCESS_COMPLETE_PLAN(true, 200, "해당 할 일을 완료하였습니다."),
    SUCCESS_INCOMPLETE_PLAN(true, 200, "해당 할 일의 완료를 취소하였습니다."),
    SUCCESS_SAVE_SEARCH_LOG(true, 200, "검색 기록 저장에 성공하였습니다"),
    SUCCESS_DELETE_TEAM(true, 200, "팀 삭제에 성공하였습니다."),

    /* 400 : Bad Request   */
    INVALID_REFRESH_TOKEN(false, 400, "유효하지 않은 리프레시 토큰입니다."),
    INVALID_SOCIAL_TYPE(false, 400, "소셜로그인 타입이 일치하지 않습니다."),
    INVALID_NICKNAME(false, 400, "닉네임 형식이 올바르지 않습니다."),
    INVALID_TEAM_MEMBER(false, 400, "그룹 호스트(자기 자신)은/는 초대할 수 없습니다."),
    INVALID_TEAM_MEMBER_SIZE(false, 400, "그룹 멤버는 최소 1명, 최대 10명으로 제한됩니다."),
    INVALID_HOST(false, 400, "해당 그룹의 호스트가 아니므로 방출, 위임 및 영구 삭제 권한이 없습니다."),
    INVALID_EMITTER(false, 400, "방출할 수 없는 유저입니다."),
    INVALID_INVITER(false, 400, "해당 유저는 초대할 수 없는 유저입니다."),
    INVALID_MANAGER(false, 400, "해당 할 일의 담당자가 아니므로 할 일을 넘길 수 없습니다."),
    INVALID_COMPLETE_PLAN(false, 400, "해당 할 일의 담당자가 아니므로 할 일을 완료할 수 없습니다."),
    INVALID_INCOMPLETE_PLAN(false, 400, "해당 할 일의 담당자가 아니므로 할 일의 완료 취소를 할 수 없습니다."),

    INVALID_PLAN(false, 400, "해당 할 일은 삭제되었습니다"),
    INVALID_SAVE_SEARCH_LOG(false, 400, "본인은 검색기록에 저장할 수 없습니다."),

    EXCEED_HOST_TEAM_SIZE(false, 400, "그룹을 더이상 생성할 수 없습니다. 그룹 개수는 최대 6개로 제한됩니다."),
    USER_NICKNAME_ALREADY_EXISTS(false, 400, "이미 사용중인 닉네임입니다."),

    USER_ALREADY_FRIEND_REQUEST(false, 400, "해당 유저에게 이미 친구 신청을 하였습니다."),
    USER_ALREADY_GET_FRIEND_REQUEST(false, 400, "나에게 이미 친구 신청을 보낸 유저입니다."),
    USER_ALREADY_EMIT(false, 400, "이미 방출된 유저입니다"),
    ALREADY_ON_FRIEND(false, 400, "이미 친구인 유저입니다."),
    ALREADY_TEAM_HOST(false, 400, "이미 해당 그룹의 호스트입니다."),
    ALREADY_ACCEPT_TEAM_INVITATION(false, 400, "이미 해당 그룹의 초대를 수락하였습니다."),
    ALREADY_INCOMPLETE_PLAN(false, 400, "해당 할 일의 완료 취소를 할 수 없습니다."),


    /* 404 : NOT FOUND */
    MEMBER_NOT_FOUND(false, 404, "해당 유저를 찾을 수 없습니다."),
    MEMBER_NOT_FOUND_IN_TEAM(false, 404, "해당 그룹에서 해당 유저를 찾을 수 없습니다."),
    EMAIL_NOT_FOUND(false, 404, "해당 이메일을 찾을 수 없습니다."),
    FRIEND_NOT_FOUND(false, 404, "해당 유저와는 친구 상태가 아니거나, 친구 신청이 존재하지 않습니다."),
    TEAM_NOT_FOUND(false, 404, "존재하지 않는 그룹입니다."),
    TEAM_INVITATION_NOT_FOUND(false, 404, "해당 그룹에 대한 초대가 존재하지 않습니다."),
    PLAN_NOT_FOUND(false, 404, "해당 할 일을 찾을 수 없습니다."),

    /* 500 */
    DATABASE_ERROR(false, 500, "데이터베이스 오류입니다.");

    private final boolean isSuccess;
    private final int code;
    private final String message;

}
