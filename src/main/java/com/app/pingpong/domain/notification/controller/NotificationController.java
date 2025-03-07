package com.app.pingpong.domain.notification.controller;

import com.app.pingpong.domain.notification.dto.request.NotificationFriendRequest;
import com.app.pingpong.domain.notification.dto.request.NotificationRequest;
import com.app.pingpong.domain.notification.dto.request.NotificationTeamRequest;
import com.app.pingpong.domain.notification.dto.response.NotificationExistResponse;
import com.app.pingpong.domain.notification.dto.response.NotificationResponse;
import com.app.pingpong.domain.notification.service.NotificationService;
import com.app.pingpong.global.aop.CheckLoginStatus;
import com.app.pingpong.global.aop.CurrentLoginMemberId;
import com.app.pingpong.global.common.exception.StatusCode;
import com.app.pingpong.global.common.response.BaseResponse;
import com.app.pingpong.global.common.status.Authority;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
}
