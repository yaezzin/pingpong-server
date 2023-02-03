package com.app.pingpong.domain.team.controller;

import com.app.pingpong.domain.member.dto.response.MemberSearchResponse;
import com.app.pingpong.domain.team.dto.request.TeamRequest;
import com.app.pingpong.domain.team.dto.response.TeamMemberResponse;
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
    @GetMapping("/{id}")
    public BaseResponse<List<TeamMemberResponse>> getTeamMembers(@PathVariable Long id) {
        return new BaseResponse<>(teamService.getTeamMembers(id));
    }

}
