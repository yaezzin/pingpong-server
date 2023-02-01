package com.app.pingpong.domain.friend.controller;

import com.app.pingpong.domain.friend.dto.request.FriendRequest;
import com.app.pingpong.domain.friend.dto.response.FriendResponse;
import com.app.pingpong.domain.friend.service.FriendService;
import com.app.pingpong.global.common.BaseResponse;
import com.app.pingpong.global.exception.StatusCode;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/friends")
public class FriendController {

    private final FriendService friendService;

    @ResponseBody
    @PostMapping("")
    public BaseResponse<FriendResponse> addFriend(@RequestBody FriendRequest request) {
        return new BaseResponse<>(friendService.addFriend(request));
    }

    @ResponseBody
    @PostMapping("/accept")
    public BaseResponse<StatusCode> acceptFriend(@RequestParam("opponentId") Long id) {
        return new BaseResponse<>(friendService.acceptFriend(id));
    }

    @ResponseBody
    @PostMapping("/refuse")
    public BaseResponse<StatusCode> refuseFriend(@RequestParam("opponentId") Long id) {
        return new BaseResponse<>(friendService.refuseFriend(id));
    }

}
