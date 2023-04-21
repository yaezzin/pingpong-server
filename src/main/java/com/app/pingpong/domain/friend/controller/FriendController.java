package com.app.pingpong.domain.friend.controller;

import com.app.pingpong.domain.friend.dto.request.FriendRequest;
import com.app.pingpong.domain.friend.dto.response.FriendResponse;
import com.app.pingpong.domain.friend.entity.Friend;
import com.app.pingpong.domain.friend.service.FriendService;
import com.app.pingpong.domain.member.dto.response.MemberResponse;
import com.app.pingpong.global.common.BaseResponse;
import com.app.pingpong.global.exception.StatusCode;
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
    public BaseResponse<StatusCode> accept(@RequestParam("opponentId") Long id) {
        return new BaseResponse<>(friendService.accept(id));
    }

    @ResponseBody
    @PostMapping("/refuse")
    public BaseResponse<StatusCode> refuse(@RequestParam("opponentId") Long id) {
        return new BaseResponse<>(friendService.refuse(id));
    }

    @ResponseBody
    @GetMapping("")
    public BaseResponse<List<MemberResponse>> getMyFriends() {
        return new BaseResponse<>(friendService.getMyFriends());
    }
}
