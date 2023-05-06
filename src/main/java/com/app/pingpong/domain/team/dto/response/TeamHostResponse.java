package com.app.pingpong.domain.team.dto.response;

import com.app.pingpong.domain.team.entity.Team;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class TeamHostResponse {
    private Long teamId;
    private String teamName;
    private Long hostId;
    private List<TeamCompactResponse> responses;

    public static TeamHostResponse of(Team team, List<TeamCompactResponse> list) {
        return new TeamHostResponse(team.getId(), team.getName(), team.getHost().getId(), list);
    }
}
