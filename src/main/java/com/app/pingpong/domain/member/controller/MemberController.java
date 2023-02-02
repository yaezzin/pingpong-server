package com.app.pingpong.domain.member.controller;

import com.app.pingpong.domain.friend.entity.Friend;
import com.app.pingpong.domain.member.dto.request.SearchLogRequest;
import com.app.pingpong.domain.member.dto.request.SignUpRequest;
import com.app.pingpong.domain.member.dto.request.UpdateRequest;
import com.app.pingpong.domain.member.dto.response.MemberDetailResponse;
import com.app.pingpong.domain.member.dto.response.MemberResponse;
import com.app.pingpong.domain.member.dto.response.MemberSearchResponse;
import com.app.pingpong.domain.member.service.MemberService;
import com.app.pingpong.global.common.BaseResponse;
import com.app.pingpong.global.exception.StatusCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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

    @ResponseBody
    @GetMapping("/{id}")
    public BaseResponse<MemberResponse> findById(@PathVariable Long id) {
        return new BaseResponse<>(memberService.findById(id));
    }

    @ResponseBody
    @PatchMapping("/{id}")
    public BaseResponse<MemberResponse> update(@PathVariable Long id, @RequestBody UpdateRequest request) {
        return new BaseResponse<>(memberService.update(id, request));
    }

    @ResponseBody
    @DeleteMapping("/{id}")
    public BaseResponse<String> delete(@PathVariable Long id) {
        return memberService.delete(id);
    }

    @ResponseBody
    @GetMapping("/{id}/mypage")
    public BaseResponse<MemberDetailResponse> getMyPage(@PathVariable Long id) {
        return new BaseResponse<>(memberService.getMyPage(id));
    }

    // 타인 프로필 조회
    @ResponseBody
    @GetMapping("/{id}/profile")
    public BaseResponse<MemberDetailResponse> getOppPage(@PathVariable Long id) {
        return new BaseResponse<>(memberService.getOppPage(id));
    }

    @ResponseBody
    @GetMapping("/friends")
    public BaseResponse<List<Friend>> getMyFriends() {
        return new BaseResponse<>(memberService.getMyFriends());
    }

    // 검색
    @GetMapping("/search")
    public BaseResponse<List<MemberSearchResponse>> findByNickname(@RequestParam("nickname") String nickname) {
        return new BaseResponse<>(memberService.findByNickname(nickname));
    }

    // 검색기록 저장
    @PostMapping("/search-log")
    public BaseResponse<StatusCode> saveSearchLog(@RequestBody SearchLogRequest request) throws IOException {
        return new BaseResponse<>(memberService.saveSearchLog(request));
    }

    // 최근 검색 기록 조회
    @GetMapping("/search-log")
    public BaseResponse<Map<Object, Object>> saveSearchLog() throws JsonProcessingException {
        return new BaseResponse<>(memberService.getSearchLog());
    }

}
