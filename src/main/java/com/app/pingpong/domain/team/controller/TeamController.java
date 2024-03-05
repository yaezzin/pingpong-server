package com.app.pingpong.domain.team.controller;

import com.app.pingpong.domain.team.dto.request.TeamAchieveRequest;
import com.app.pingpong.domain.team.dto.request.TeamPlanPassRequest;
import com.app.pingpong.domain.team.dto.request.TeamPlanRequest;
import com.app.pingpong.domain.team.dto.request.TeamRequest;
import com.app.pingpong.domain.team.dto.response.*;
import com.app.pingpong.domain.team.service.TeamService;
import com.app.pingpong.global.aop.CheckLoginStatus;
import com.app.pingpong.global.aop.CurrentLoginMemberId;
import com.app.pingpong.global.common.exception.StatusCode;
import com.app.pingpong.global.common.response.BaseResponse;
import com.app.pingpong.global.common.status.Authority;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService teamService;

    @ResponseBody
    @PostMapping("")
    public BaseResponse<TeamResponse> create(@RequestBody TeamRequest request) {
        return new BaseResponse<>(teamService.create(request));
    }

    @ResponseBody
    @DeleteMapping("/{id}")
    public BaseResponse<StatusCode> delete(@PathVariable Long id) {
        return new BaseResponse<>(teamService.delete(id));
    }

    @ResponseBody
    @PatchMapping("/{id}")
    public BaseResponse<TeamResponse> update(@PathVariable Long id, @RequestBody TeamRequest request) {
        return new BaseResponse<>(teamService.update(id, request));
    }

    @ResponseBody
    @PatchMapping("/{teamId}/host")
    @CheckLoginStatus(auth = Authority.ROLE_USER)
    public BaseResponse<TeamHostResponse> updateHost(@PathVariable("teamId") Long teamId, @RequestParam Long delegatorId, @CurrentLoginMemberId Long loginMemberId) {
        return new BaseResponse<>(teamService.updateHost(teamId, delegatorId, loginMemberId));
    }

    @ResponseBody
    @PatchMapping("/{id}/emit")
    public BaseResponse<TeamHostResponse> emit(@PathVariable("id") Long teamId, @RequestParam Long emitterId) {
        return new BaseResponse<>(teamService.emit(teamId, emitterId));
    }

    @ResponseBody
    @GetMapping("/{id}/members")
    public BaseResponse<List<TeamMemberResponse>> getTeamMembers(@PathVariable("id") Long id) {
        return new BaseResponse<>(teamService.getTeamMembers(id));
    }

    @ResponseBody
    @PostMapping("/{id}/accept")
    public BaseResponse<StatusCode> accept(@PathVariable("id") Long teamId) {
        return new BaseResponse<>(teamService.accept(teamId));
    }

    @ResponseBody
    @DeleteMapping("/{id}/refuse")
    public BaseResponse<StatusCode> refuse(@PathVariable("id") Long teamId) {
        return new BaseResponse<>(teamService.refuse(teamId));
    }

    @ResponseBody
    @PostMapping("/{id}/resign")
    @CheckLoginStatus(auth = Authority.ROLE_USER)
    public BaseResponse<StatusCode> resign(@PathVariable("id") Long teamId, @CurrentLoginMemberId Long id) {
        return new BaseResponse<>(teamService.resign(teamId, id));
    }

    @ResponseBody
    @PostMapping("/{id}/plans")
    public BaseResponse<TeamPlanResponse> createPlan(@PathVariable("id") Long id, @RequestBody TeamPlanRequest request) {
        return new BaseResponse<>(teamService.createPlan(id, request));
    }

    @ResponseBody
    @DeleteMapping("/{teamId}/plans/{planId}")
    public BaseResponse<TeamPlanResponse> deletePlan(@PathVariable("teamId") Long teamId, @PathVariable("planId") Long planId) {
        return new BaseResponse<>(teamService.deletePlan(teamId, planId));
    }

    @ResponseBody
    @PatchMapping("/{teamId}/plans/pass")
    public BaseResponse<TeamPlanResponse> passPlan(@PathVariable("teamId") Long teamId,
                                                   @CurrentLoginMemberId Long loginMemberId,
                                                   @RequestBody TeamPlanPassRequest request) {
        return new BaseResponse<>(teamService.passPlan(teamId, loginMemberId, request));
    }

    @ResponseBody
    @PatchMapping("/{teamId}/plans/{planId}/complete")
    @CheckLoginStatus(auth = Authority.ROLE_USER)
    public BaseResponse<StatusCode> completePlan(@PathVariable("teamId") Long teamId,
                                                 @PathVariable("planId") Long planId,
                                                 @CurrentLoginMemberId Long id) {
        return new BaseResponse<>(teamService.completePlan(teamId, planId, id));
    }

    @ResponseBody
    @PatchMapping("/{teamId}/plans/{planId}/incomplete")
    @CheckLoginStatus(auth = Authority.ROLE_USER)
    public BaseResponse<StatusCode> incompletePlan(@PathVariable("teamId") Long teamId,
                                                   @PathVariable("planId") Long planId,
                                                   @CurrentLoginMemberId Long id) {
        return new BaseResponse<>(teamService.incompletePlan(teamId, planId, id));
    }

    @ResponseBody
    @GetMapping("/{id}/calendars")
    public BaseResponse<TeamPlanDetailResponse> getTeamCalendarByDate(@PathVariable Long id,
                                                                      @RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
                                                                      @CurrentLoginMemberId Long loginMemberId) {
        return new BaseResponse<>(teamService.getTeamCalendarByDate(id, date, loginMemberId));
    }

    @ResponseBody
    @PostMapping("/{id}/calendars/achievement")
    public BaseResponse<List<TeamAchieveResponse>> getTeamAchievementRate(@PathVariable Long id,
                                                                          @RequestBody TeamAchieveRequest request) {
        return new BaseResponse<>(teamService.getTeamAchievementRate(id, request));
    }

    @ResponseBody
    @GetMapping("/{id}/trash")
    @CheckLoginStatus(auth = Authority.ROLE_USER)
    public BaseResponse<List<TeamPlanResponse>> getTrash(@PathVariable("id") Long teamId, @CurrentLoginMemberId Long id) {
        return new BaseResponse<>(teamService.getTrash(teamId, id));
    }

    @ResponseBody
    @DeleteMapping("/{id}/all-trash")
    @CheckLoginStatus(auth = Authority.ROLE_USER)
    public BaseResponse<StatusCode> deleteAllTrash(@PathVariable("id") Long teamId, @CurrentLoginMemberId Long id) {
        return new BaseResponse<>(teamService.deleteAllTrash(teamId, id));
    }

    @ResponseBody
    @DeleteMapping("/{teamId}/trash/{planId}")
    @CheckLoginStatus(auth = Authority.ROLE_USER)
    public BaseResponse<StatusCode> deleteTrash(@PathVariable("teamId") Long teamId,
                                                @PathVariable("planId") Long planId,
                                                @CurrentLoginMemberId Long id) {
        return new BaseResponse<>(teamService.deleteTrash(teamId, planId, id));
    }

    @ResponseBody
    @PatchMapping("/{teamId}/trash/{planId}")
    @CheckLoginStatus(auth = Authority.ROLE_USER)
    public BaseResponse<StatusCode> recoverTrash(@PathVariable("teamId") Long teamId,
                                                 @PathVariable("planId") Long planId,
                                                 @CurrentLoginMemberId Long id) {
        return new BaseResponse<>(teamService.recoverTrash(teamId, planId, id));
    }
}
