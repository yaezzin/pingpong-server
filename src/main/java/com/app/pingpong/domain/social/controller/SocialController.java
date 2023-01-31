package com.app.pingpong.domain.social.controller;

import com.app.pingpong.domain.social.dto.request.MemberInfoRequest;
import com.app.pingpong.domain.social.dto.request.MemberLoginRequest;
import com.app.pingpong.domain.social.dto.response.MemberInfoResponse;
import com.app.pingpong.domain.social.dto.response.MemberLoginResponse;
import com.app.pingpong.domain.social.service.SocialService;
import com.app.pingpong.global.common.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/oauth")
public class SocialController {

    private final SocialService socialService;

    @ResponseBody
    @GetMapping("/info")
    public BaseResponse<MemberInfoResponse> getUserInfo(@RequestBody MemberInfoRequest request) {
        return new BaseResponse<>(socialService.getUserInfo(request));
    }

    @ResponseBody
    @PostMapping("/login")
    public BaseResponse<MemberLoginResponse> login(@RequestBody MemberLoginRequest request) {
        return new BaseResponse<>(socialService.login(request));
    }
}
