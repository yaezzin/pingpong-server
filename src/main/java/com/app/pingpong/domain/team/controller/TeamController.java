package com.app.pingpong.domain.team.controller;

import com.app.pingpong.domain.team.dto.request.TeamRequest;
import com.app.pingpong.domain.team.dto.response.TeamResponse;
import com.app.pingpong.domain.team.service.TeamService;
import com.app.pingpong.global.common.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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

}
