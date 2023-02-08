package com.app.pingpong.domain.team.controller;

import com.app.pingpong.domain.member.dto.response.MemberSearchResponse;
import com.app.pingpong.domain.team.dto.request.TeamPlanPassRequest;
import com.app.pingpong.domain.team.dto.request.TeamPlanRequest;
import com.app.pingpong.domain.team.dto.request.TeamRequest;
import com.app.pingpong.domain.team.dto.response.TeamHostResponse;
import com.app.pingpong.domain.team.dto.response.TeamMemberResponse;
import com.app.pingpong.domain.team.dto.response.TeamPlanResponse;
import com.app.pingpong.domain.team.dto.response.TeamResponse;
import com.app.pingpong.domain.team.service.TeamService;
import com.app.pingpong.global.common.BaseResponse;
import com.app.pingpong.global.exception.StatusCode;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
    @GetMapping("/{id}/members")
    public BaseResponse<List<TeamMemberResponse>> getTeamMembers(@PathVariable Long id) {
        return new BaseResponse<>(teamService.getTeamMembers(id));
    }

    @ResponseBody
    @PatchMapping("/{id}/host")
    public BaseResponse<TeamHostResponse> updateHost(@PathVariable("id") Long id, @RequestParam Long delegatorId) {
        return new BaseResponse<>(teamService.updateHost(id, delegatorId));
    }

    @ResponseBody
    @PatchMapping("/{id}/emit")
    public BaseResponse<TeamHostResponse> emit(@PathVariable("id") Long teamId, @RequestParam Long emitterId) {
        return new BaseResponse<>(teamService.emit(teamId, emitterId));
    }

    @ResponseBody
    @PostMapping("/{id}/accept")
    public BaseResponse<StatusCode> accept(@PathVariable("id") Long teamId) {
        return new BaseResponse<>(teamService.accept(teamId));
    }

    @ResponseBody
    @PostMapping("/refuse")
    public BaseResponse<StatusCode> refuse(@PathVariable("id") Long teamId) {
        return new BaseResponse<>(teamService.refuse(teamId));
    }

    @ResponseBody
    @PostMapping("/{id}/plans")
    public BaseResponse<TeamPlanResponse> createPlan(@PathVariable Long id, @RequestBody TeamPlanRequest request) {
        return new BaseResponse<>(teamService.createPlan(id, request));
    }

    @ResponseBody
    @DeleteMapping("/{teamId}/plans/{planId}")
    public BaseResponse<TeamPlanResponse> deletePlan(@PathVariable("teamId") Long teamId, @PathVariable("planId") Long planId) {
        return new BaseResponse<>(teamService.deletePlan(teamId, planId));
    }

    @ResponseBody
    @PatchMapping("/{teamId}/plans/pass")
    public BaseResponse<TeamPlanResponse> passPlan(@PathVariable("teamId") Long teamId, @RequestBody TeamPlanPassRequest request) {
        return new BaseResponse<>(teamService.passPlan(teamId, request));
    }
}
