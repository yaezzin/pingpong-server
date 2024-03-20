package com.app.pingpong.global.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StatusCode {

    SUCCESS(true, 200, "요청에 성공하였습니다.", "SUCCESS"),
    SUCCESS_VALIDATE_NICKNAME(true, 200, "닉네임 형식 확인에 성공하였습니다.", "SUCCESS_VALIDATE_NICKNAME"),
    SUCCESS_DELETE_MEMBER(true, 200, "유저 탈퇴에 성공하였습니다.", "SUCCESS_DELETE_MEMBER"),
    SUCCESS_DELETE_AWS_S3(true, 200, "S3에서 사진 삭제를 성공하였습니다.", "SUCCESS_DELETE_AWS_S3"),
    SUCCESS_ACCEPT_FRIEND(true, 200, "친구 신청 수락에 성공하였습니다.", "SUCCESS_ACCEPT_FRIEND"),
    SUCCESS_REFUSE_FRIEND(true, 200, "친구 신청 거절에 성공하였습니다.", "SUCCESS_REFUSE_FRIEND"),
    SUCCESS_LOGOUT(true, 200, "로그아웃에 성공하였습니다.", "SUCCESS_LOGOUT"),
    SUCCESS_ACCEPT_TEAM_INVITATION(true, 200, "그룹 초대 수락에 성공하였습니다.", "SUCCESS_ACCEPT_TEAM_INVITATION"),
    SUCCESS_REFUSE_TEAM_INVITATION(true, 200, "그룹 초대 거절에 성공하였습니다.", "SUCCESS_REFUSE_TEAM_INVITATION"),
    SUCCESS_DELETE_ALL_TRASH(true, 200, "해당 그룹의 휴지통 전체 삭제에 성공하였습니다.", "SUCCESS_DELETE_ALL_TRASH"),
    SUCCESS_DELETE_TRASH(true, 200, "해당 그룹의 휴지통에서 할 일(1개) 삭제에 성공하였습니다.", "SUCCESS_DELETE_TRASH"),
    SUCCESS_RECOVER_TRASH(true, 200, "해당 할 일을 휴지통에서 복구하였습니다.", "SUCCESS_RECOVER_TRASH"),
    SUCCESS_COMPLETE_PLAN(true, 200, "해당 할 일을 완료하였습니다.", "SUCCESS_COMPLETE_PLAN"),
    SUCCESS_INCOMPLETE_PLAN(true, 200, "해당 할 일의 완료를 취소하였습니다.", "SUCCESS_INCOMPLETE_PLAN"),
    SUCCESS_SAVE_SEARCH_LOG(true, 200, "검색 기록 저장에 성공하였습니다", "SUCCESS_SAVE_SEARCH_LOG"),
    SUCCESS_DELETE_TEAM(true, 200, "팀 삭제에 성공하였습니다.", "SUCCESS_DELETE_TEAM"),
    SUCCESS_RESIGN_TEAM(true, 200, "팀 나가기에 성공하였습니다.", "SUCCESS_RESIGN_TEAM"),
    SUCCESS_SEND_NOTIFICATION(true, 200, "알림 전송을 성공하였습니다.", "SUCCESS_SEND_NOTIFICATION"),
    SUCCESS_EXISTS_NOTIFY(true, 200, "모든 알림을 읽었습니다.", "SUCCESS_NOT_EXISTS_UNREAD_NOTIFY"),
    SUCCESS_EXISTS_UNREAD_NOTIFY(true, 200, "읽지 않은 알림이 존재합니다", "SUCCESS_EXISTS_UNREAD_NOTIFY"),
    SUCCESS_UNFOLLOW_FRIEND(true, 200, "친구 끊기에 성공하였습니다", "SUCCESS_UNFOLLOW_FRIEND"),
    SUCCESS_DELETE_ADMIN_POST(true, 200, "공지사항 삭제에 성공하였습니다.", "SUCCESS_DELETE_ADMIN_POST"),

    /* 400 : Bad Request   */
    INVALID_REFRESH_TOKEN(false, 400, "유효하지 않은 리프레시 토큰입니다.", "INVALID_REFRESH_TOKEN"),
    INVALID_SOCIAL_TYPE(false, 400, "소셜로그인 타입이 일치하지 않습니다.", "INVALID_SOCIAL_TYPE"),
    INVALID_NICKNAME(false, 400, "닉네임 형식이 올바르지 않습니다.", "INVALID_NICKNAME"),
    INVALID_TEAM_HOST_MEMBER(false, 400, "방장의 ID를 포함하여 그룹을 생성할 수 없습니다.", "INVALID_TEAM_HOST_MEMBER"),
    INVALID_TEAM_MEMBER(false, 400, "그룹 호스트(자기 자신)은/는 초대할 수 없습니다.", "INVALID_TEAM_MEMBER"),
    INVALID_TEAM_MEMBER_SIZE(false, 400, "그룹 멤버는 최소 1명, 최대 10명으로 제한됩니다.", "INVALID_TEAM_MEMBER_SIZE"),
    INVALID_HOST(false, 400, "해당 그룹의 호스트가 아니므로 방출, 위임 및 영구 삭제 권한이 없습니다.", "INVALID_HOST"),
    INVALID_EMITTER(false, 400, "방출할 수 없는 유저입니다.", "INVALID_EMITTER"),
    INVALID_INVITER(false, 400, "해당 유저는 초대할 수 없는 유저입니다.", "INVALID_INVITER"),
    INVALID_MANAGER(false, 400, "해당 할 일의 담당자가 아니므로 할 일을 넘길 수 없습니다.", "INVALID_MANAGER"),
    INVALID_COMPLETE_PLAN(false, 400, "해당 할 일의 담당자가 아니므로 할 일을 완료할 수 없습니다.", "INVALID_COMPLETE_PLAN"),
    INVALID_INCOMPLETE_PLAN(false, 400, "해당 할 일의 담당자가 아니므로 할 일의 완료 취소를 할 수 없습니다.", "INVALID_INCOMPLETE_PLAN"),
    INVALID_RESIGN(false, 400, "그룹의 호스트는 팀을 나갈 수 없습니다.", "INVALID_RESIGN"),
    INVALID_RESIGN_STATUS(false, 400, "참여하지 않은 그룹이거나, 이미 나간 그룹입니다.", "INVALID_RESIGN_STATUS"),
    INVALID_S3_URL(false, 400, "올바르지 않은 S3 URL이므로 삭제가 불가능합니다.", "INVALID_S3_URL"),

    INVALID_PLAN(false, 400, "해당 할 일은 삭제되었습니다", "INVALID_PLAN"),
    INVALID_SAVE_SEARCH_LOG(false, 400, "본인은 검색기록에 저장할 수 없습니다.", "INVALID_SAVE_SEARCH_LOG"),

    INVALID_LOGOUT_EMAIL(false, 400, "로그아웃 할 수 없는 이메일 입니다.", "INVALID_LOGOUT_EMAIL"),
    INVALID_ACCESS_TOKEN(false, 400, "유효하지 않은 토큰입니다.", "INVALID_ACCESS_TOKEN"),

    EXCEED_HOST_TEAM_SIZE(false, 400, "그룹을 더이상 생성할 수 없습니다. 그룹 개수는 최대 6개로 제한됩니다.", "EXCEED_HOST_TEAM_SIZE"),
    EXCEED_MEMBER_TEAM_THRESHOLD(false, 400, "참여한 그룹이 6개 이상이므로, 그룹 생성 또는 참여가 불가능합니다.", "EXCEED_MEMBER_TEAM_THRESHOLD"),
    MEMBER_NICKNAME_ALREADY_EXISTS(false, 400, "이미 사용중인 닉네임입니다.", "MEMBER_NICKNAME_ALREADY_EXISTS"),

    MEMBER_ALREADY_FRIEND_REQUEST(false, 400, "해당 유저에게 이미 친구 신청을 하였습니다.", "MEMBER_ALREADY_FRIEND_REQUEST"),
    MEMBER_ALREADY_GET_FRIEND_REQUEST(false, 400, "나에게 이미 친구 신청을 보낸 유저입니다.", "MEMBER_ALREADY_GET_FRIEND_REQUEST"),
    MEMBER_ALREADY_EMIT(false, 400, "이미 방출된 유저입니다", "MEMBER_ALREADY_EMIT"),
    ALREADY_ON_FRIEND(false, 400, "이미 친구인 유저입니다.", "ALREADY_ON_FRIEND"),
    ALREADY_TEAM_HOST(false, 400, "이미 해당 그룹의 호스트입니다.", "ALREADY_TEAM_HOST"),
    ALREADY_ACCEPT_TEAM_INVITATION(false, 400, "이미 해당 그룹의 초대를 수락하였습니다.", "ALREADY_ACCEPT_TEAM_INVITATION"),
    ALREADY_COMPLETE_PLAN(false, 400, "이미 완료된 일정입니다.", "ALREADY_COMPLETE_PLAN"),
    ALREADY_INCOMPLETE_PLAN(false, 400, "완료된 일정이 아니기 떄문에, 해당 일정의 완료 취소를 할 수 없습니다.", "ALREADY_INCOMPLETE_PLAN"),
    ALREADY_INVITE_TEAM(false, 400, "이미 초대했거나, 이미 해당 그룹에 참가한 유저입니다.", "ALREADY_INVITE_TEAM"),
    ALREADY_DELETE_TRASH(false, 400, "이미 영구 삭제된 일정입니다.", "ALREADY_DELETE_TRASH"),

    /* 404 : NOT FOUND */
    MEMBER_NOT_FOUND(false, 404, "해당 유저를 찾을 수 없습니다.", "MEMBER_NOT_FOUND"),
    MEMBER_NOT_FOUND_IN_TEAM(false, 404, "해당 그룹에서 해당 유저를 찾을 수 없습니다.", "MEMBER_NOT_FOUND_IN_TEAM"),
    DELEGATOR_NOT_FOUND_IN_TEAM(false, 404, "위임하려고 하는 유저가 해당 그룹에 존재하지 않습니다.", "DELEGATOR_NOT_FOUND_IN_TEAM"),
    EMAIL_NOT_FOUND(false, 404, "해당 이메일을 찾을 수 없습니다.", "EMAIL_NOT_FOUND"),
    FRIEND_NOT_FOUND(false, 404, "해당 유저와는 친구 상태가 아니거나, 친구 신청이 존재하지 않습니다.", "FRIEND_NOT_FOUND"),
    TEAM_NOT_FOUND(false, 404, "존재하지 않는 그룹입니다.", "TEAM_NOT_FOUND"),
    TEAM_INVITATION_NOT_FOUND(false, 404, "해당 그룹에 대한 초대가 존재하지 않습니다.", "TEAM_INVITATION_NOT_FOUND"),
    PLAN_NOT_FOUND(false, 404, "해당 할 일을 찾을 수 없습니다.", "PLAN_NOT_FOUND"),
    NOTIFICATION_NOT_FOUND(false, 404, "알림이 존재하지 않습니다.", "NOTIFICATION_NOT_FOUND"),
    BADGE_NOT_FOUND(false, 404, "해당 뱃지는 존재하지 않습니다", "BADGE_NOT_FOUND"),
    PLANS_IN_TRASH_NOT_FOUND(false, 404, "휴지통에서 삭제할 일정이 존재하지 않습니다.", "PLANS_IN_TRASH_NOT_FOUND"),
    ADMiN_POST_NOT_FOUND(false, 404, "해당 공지를 찾을 수 없습니다.", "ADMiN_POST_NOT_FOUND"),

    /* 500 */
    DATABASE_ERROR(false, 500, "데이터베이스 오류입니다.", "DATABASE_ERROR");

    private final boolean isSuccess;
    private final int code;
    private final String message;
    private final String type;
}
