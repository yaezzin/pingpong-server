package com.app.pingpong.domain.social.controller;

import com.app.pingpong.domain.social.dto.request.MemberInfoRequest;
import com.app.pingpong.domain.social.dto.request.MemberLoginRequest;
import com.app.pingpong.domain.social.dto.request.MemberLogoutRequest;
import com.app.pingpong.domain.social.dto.request.TokenRequest;
import com.app.pingpong.domain.social.dto.response.MemberInfoResponse;
import com.app.pingpong.domain.social.dto.response.MemberLoginResponse;
import com.app.pingpong.domain.social.dto.response.TokenResponse;
import com.app.pingpong.domain.social.service.SocialService;
import com.app.pingpong.global.common.response.BaseResponse;
import com.app.pingpong.global.common.exception.StatusCode;
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

    @ResponseBody
    @PostMapping("/logout")
    public BaseResponse<StatusCode> logout(@RequestBody MemberLogoutRequest request) {
        return new BaseResponse<>(socialService.logout(request));
    }

    @ResponseBody
    @PostMapping("/reissue")
    public BaseResponse<TokenResponse> reissue(@RequestBody TokenRequest request) {
        return new BaseResponse<>(socialService.reissue(request));
    }
}
