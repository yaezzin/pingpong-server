package com.app.pingpong.domain.social.controller;

import com.app.pingpong.domain.social.dto.request.UserInfoRequest;
import com.app.pingpong.domain.social.dto.response.UserInfoResponse;
import com.app.pingpong.domain.social.service.SocialService;
import com.app.pingpong.global.common.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/oauth")
public class SocialController {

    private final SocialService socialService;

    @ResponseBody
    @GetMapping("/info")
    public BaseResponse<UserInfoResponse> getUserInfo(@RequestBody UserInfoRequest request) {
        return new BaseResponse<>(socialService.getUserInfo(request));
    }
}
