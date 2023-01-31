package com.app.pingpong.domain.member.controller;

import com.app.pingpong.domain.member.dto.request.SignUpRequest;
import com.app.pingpong.domain.member.dto.request.UpdateRequest;
import com.app.pingpong.domain.member.dto.response.MemberResponse;
import com.app.pingpong.domain.member.service.MemberService;
import com.app.pingpong.global.common.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    @ResponseBody
    @PostMapping("/sign-up")
    public BaseResponse<MemberResponse> signup(@RequestBody SignUpRequest request) {
        return new BaseResponse<>(memberService.signup(request));
    }

    @ResponseBody
    @PostMapping("/validate")
    public BaseResponse<String> validate(@RequestParam String nickname) {
        return memberService.validateNickname(nickname);
    }

    // 수정, 탈퇴, 로그아웃, 회원 정보 id로 찾기
    @ResponseBody
    @PatchMapping("/{id}")
    public BaseResponse<MemberResponse> update(@PathVariable Long id, @RequestBody UpdateRequest request) {
        return new BaseResponse<>(memberService.update(id, request));
    }
}
