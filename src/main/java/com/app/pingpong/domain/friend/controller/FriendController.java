package com.app.pingpong.domain.friend.controller;

import com.app.pingpong.domain.friend.dto.request.FriendRefuseRequest;
import com.app.pingpong.domain.friend.dto.request.FriendRequest;
import com.app.pingpong.domain.friend.dto.response.FriendResponse;
import com.app.pingpong.domain.friend.service.FriendService;
import com.app.pingpong.domain.member.dto.response.MemberResponse;
import com.app.pingpong.global.aop.CheckLoginStatus;
import com.app.pingpong.global.aop.CurrentLoginMemberId;
import com.app.pingpong.global.common.exception.StatusCode;
import com.app.pingpong.global.common.response.BaseResponse;
import com.app.pingpong.global.common.status.Authority;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/friends")
public class FriendController {

    private final FriendService friendService;

    @ResponseBody
    @PostMapping("/apply")
    public BaseResponse<FriendResponse> apply(@RequestBody FriendRequest request) {
        return new BaseResponse<>(friendService.apply(request));
    }

    @ResponseBody
    @PostMapping("/accept")
    @CheckLoginStatus(auth = Authority.ROLE_USER)
    public BaseResponse<StatusCode> accept(@RequestParam("opponentId") Long opponentId, @CurrentLoginMemberId Long id) {
        return new BaseResponse<>(friendService.accept(opponentId, id));
    }

    @ResponseBody
    @PostMapping("/refuse")
    @CheckLoginStatus(auth = Authority.ROLE_USER)
    public BaseResponse<StatusCode> refuse(@RequestBody FriendRefuseRequest request, @CurrentLoginMemberId Long id) {
        return new BaseResponse<>(friendService.refuse(request, id));
    }

    @ResponseBody
    @GetMapping("")
    @CheckLoginStatus(auth = Authority.ROLE_USER)
    public BaseResponse<List<MemberResponse>> getMyFriends(@CurrentLoginMemberId Long id) {
        return new BaseResponse<>(friendService.getMyFriends(id));
    }
}
