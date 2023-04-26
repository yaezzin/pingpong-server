package com.app.pingpong.domain.notification.controller;

import com.app.pingpong.domain.notification.dto.request.NotificationFriendRequest;
import com.app.pingpong.domain.notification.dto.request.NotificationRequest;
import com.app.pingpong.domain.notification.dto.response.NotificationFriendResponse;
import com.app.pingpong.domain.notification.dto.response.NotificationTodoResponse;
import com.app.pingpong.domain.notification.entity.Notification;
import com.app.pingpong.domain.notification.service.NotificationService;
import com.app.pingpong.global.aop.CheckLoginStatus;
import com.app.pingpong.global.aop.CurrentLoginMemberId;
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
    public BaseResponse<NotificationTodoResponse> notifyTodo(@RequestBody NotificationRequest request, @CurrentLoginMemberId Long id) {
        return new BaseResponse<>(notificationService.notifyTodo(request, id));
    }

    @PostMapping("/friends")
    @CheckLoginStatus(auth = Authority.ROLE_USER)
    public BaseResponse<NotificationFriendResponse> notifyFriend(@RequestBody NotificationFriendRequest request, @CurrentLoginMemberId Long id) {
        return new BaseResponse<>(notificationService.notifyFriend(request, id));
    }

    @GetMapping("")
    public BaseResponse<List<Notification>> findAll() {
        return new BaseResponse<>(notificationService.findAll());
    }


}
