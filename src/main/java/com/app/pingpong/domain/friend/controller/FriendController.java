package com.app.pingpong.domain.friend.controller;

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

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/friends")
public class FriendController {

    private final FriendService friendService;

    @ResponseBody
    @PostMapping("/apply")
    public BaseResponse<FriendResponse> apply(@RequestBody FriendRequest request) throws IOException {
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
    public BaseResponse<StatusCode> refuse(@RequestParam("opponentId") Long opponentId, @CurrentLoginMemberId Long id) {
        return new BaseResponse<>(friendService.refuse(opponentId, id));
    }

    @ResponseBody
    @GetMapping("")
    @CheckLoginStatus(auth = Authority.ROLE_USER)
    public BaseResponse<List<MemberResponse>> getMyFriends(@CurrentLoginMemberId Long id) {
        return new BaseResponse<>(friendService.getMyFriends(id));
    }

    @ResponseBody
    @DeleteMapping("/unfollow")
    @CheckLoginStatus(auth = Authority.ROLE_USER)
    public BaseResponse<StatusCode> unfollow(@RequestParam("memberId") Long memberId, @CurrentLoginMemberId Long loginMemberId) {
        return new BaseResponse<>(friendService.unfollow(memberId, loginMemberId));
    }
}
