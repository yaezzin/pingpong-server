package com.app.pingpong.domain.member.dto.response;

import com.app.pingpong.domain.team.dto.response.TeamPlanResponse;
import com.app.pingpong.domain.team.entity.Team;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MemberPlanDetailResponse {
    private Long teamId;
    private String teamName;
    private Long hostId;
    private List<TeamPlanResponse> planList;

    public static MemberPlanDetailResponse of(Team team, List<TeamPlanResponse> planList) {
        return new MemberPlanDetailResponse(team.getId(), team.getName(), team.getHost().getId(), planList);
    }
}
