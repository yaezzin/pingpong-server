package com.app.pingpong.domain.member.controller;

import com.app.pingpong.domain.friend.entity.Friend;
import com.app.pingpong.domain.member.dto.request.MemberAchieveRequest;
import com.app.pingpong.domain.member.dto.request.SearchLogRequest;
import com.app.pingpong.domain.member.dto.request.SignUpRequest;
import com.app.pingpong.domain.member.dto.request.UpdateRequest;
import com.app.pingpong.domain.member.dto.response.*;
import com.app.pingpong.domain.member.service.MemberService;
import com.app.pingpong.global.aop.CheckLoginStatus;
import com.app.pingpong.global.aop.CurrentLoginMemberId;
import com.app.pingpong.global.common.Authority;
import com.app.pingpong.global.common.BaseResponse;
import com.app.pingpong.global.exception.StatusCode;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.Check;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

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

    @ResponseBody
    @GetMapping("/{id}/profile")
    public BaseResponse<MemberDetailResponse> getOppPage(@PathVariable Long id) {
        return new BaseResponse<>(memberService.getOppPage(id));
    }

    @ResponseBody
    @GetMapping("/search")
    public BaseResponse<List<MemberSearchResponse>> findByNickname(@RequestParam("nickname") String nickname) {
        return new BaseResponse<>(memberService.findByNickname(nickname));
    }

    @ResponseBody
    @PostMapping("/search-log")
    @CheckLoginStatus(auth = Authority.ROLE_USER)
    public BaseResponse<StatusCode> saveSearchLog(@RequestBody SearchLogRequest request, Long id) {
        return new BaseResponse<>(memberService.saveSearchLog(request, id));
    }

    @ResponseBody
    @GetMapping("/search-log")
    @CheckLoginStatus(auth = Authority.ROLE_USER)
    public BaseResponse<List<Object>> saveSearchLog(@CurrentLoginMemberId Long id) {
        return new BaseResponse<>(memberService.getSearchLog(id));
    }

    @ResponseBody
    @GetMapping("/teams")
    @CheckLoginStatus(auth = Authority.ROLE_USER)
    public BaseResponse<List<MemberTeamResponse>> getMemberTeams(@CurrentLoginMemberId Long id) {
        return new BaseResponse<>(memberService.getMemberTeams(id));
    }

    @ResponseBody
    @GetMapping("/calendars/achievement")
    @CheckLoginStatus(auth = Authority.ROLE_USER)
    public BaseResponse<List<MemberAchieveResponse>> getMemberAchievement(@RequestBody MemberAchieveRequest request,
                                                                          @CurrentLoginMemberId Long id) {
        return new BaseResponse<>(memberService.getMemberAchievementRate(request, id));
    }

    @ResponseBody
    @GetMapping("/calendars")
    public BaseResponse<List<MemberPlanDetailResponse>> getMemberCalendarByDate(@RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
                                                                                @CurrentLoginMemberId Long id) {
        return new BaseResponse<>(memberService.getMemberCalendarByDate(date, id));
    }
}
