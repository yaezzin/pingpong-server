package com.app.pingpong.domain.notification.controller;

import com.app.pingpong.domain.notification.dto.request.NotificationFriendRequest;
import com.app.pingpong.domain.notification.dto.request.NotificationRequest;
import com.app.pingpong.domain.notification.dto.request.NotificationTeamRequest;
import com.app.pingpong.domain.notification.dto.response.NotificationExistResponse;
import com.app.pingpong.domain.notification.dto.response.NotificationResponse;
import com.app.pingpong.domain.notification.dto.response.SSENotificationsResponse;
import com.app.pingpong.domain.notification.service.NotificationService;
import com.app.pingpong.global.aop.CheckLoginStatus;
import com.app.pingpong.global.aop.CurrentLoginMemberId;
import com.app.pingpong.global.common.exception.StatusCode;
import com.app.pingpong.global.common.response.BaseResponse;
import com.app.pingpong.global.common.status.Authority;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/to-do")
    @CheckLoginStatus(auth = Authority.ROLE_USER)
    public BaseResponse<StatusCode> notifyTodo(@RequestBody NotificationRequest request, @CurrentLoginMemberId Long id) {
        return new BaseResponse<>(notificationService.notifyTodo(request, id));
    }

    @PostMapping("/friends")
    @CheckLoginStatus(auth = Authority.ROLE_USER)
    public BaseResponse<StatusCode> notifyFriend(@RequestBody NotificationFriendRequest request, @CurrentLoginMemberId Long id) {
        return new BaseResponse<>(notificationService.notifyFriend(request, id));
    }

    @PostMapping("/teams")
    @CheckLoginStatus(auth = Authority.ROLE_USER)
    public BaseResponse<StatusCode> notifyTeam(@RequestBody NotificationTeamRequest request, @CurrentLoginMemberId Long id) {
        return new BaseResponse<>(notificationService.notifyTeam(request, id));
    }

    @PostMapping("/host")
    @CheckLoginStatus(auth = Authority.ROLE_USER)
    public BaseResponse<StatusCode> notifyHost(@RequestBody NotificationTeamRequest request, @CurrentLoginMemberId Long id) {
        return new BaseResponse<>(notificationService.notifyHost(request, id));
    }

    @PostMapping("/emit")
    @CheckLoginStatus(auth = Authority.ROLE_USER)
    public BaseResponse<StatusCode> notifyEmit(@RequestBody NotificationTeamRequest request, @CurrentLoginMemberId Long id) {
        return new BaseResponse<>(notificationService.notifyEmit(request, id));
    }

    @GetMapping("")
    @CheckLoginStatus(auth = Authority.ROLE_USER)
    public BaseResponse<List<NotificationResponse>> findAll(@CurrentLoginMemberId Long id) {
        return new BaseResponse<>(notificationService.findAll(id));
    }

    @GetMapping("/un-read")
    @CheckLoginStatus(auth = Authority.ROLE_USER)
    public BaseResponse<NotificationExistResponse> existUnReadNotification(@CurrentLoginMemberId Long id) {
        return new BaseResponse<>(notificationService.existUnReadNotification(id));
    }

    /**
     * @title 로그인 한 유저 sse 연결
     */
    @GetMapping(value = "/subscribe", produces = "text/event-stream")
    public SseEmitter subscribe(@CurrentLoginMemberId Long id,
                                @RequestHeader(value = "Last-Event-ID", required = false, defaultValue = "") String lastEventId) {
        return notificationService.subscribe(id, lastEventId);
    }

    /**
     * @title 로그인 한 유저 sse 연결
     */
    @PostMapping("send")
    public void send(@CurrentLoginMemberId Long id,
                     @RequestParam("content") String content) {
        notificationService.send(id, content);
    }

    /**
     * @title 로그인 한 유저의 모든 알림 조회
     */
    @GetMapping("get")
    public SSENotificationsResponse get(@CurrentLoginMemberId Long id) {
        return notificationService.findAllById(id);
    }

    /**
     * @title 알림 읽음 상태 변경
     */
    @PatchMapping("/read/{id}")
    public void readNotification(@PathVariable String id) {
        notificationService.readNotification(id);
    }
}
