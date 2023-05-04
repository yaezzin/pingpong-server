package com.app.pingpong.domain.team.dto.response;

import com.app.pingpong.domain.member.dto.response.MemberResponse;
import com.app.pingpong.domain.team.entity.Team;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
@AllArgsConstructor
public class TeamPlanDetailResponse {
    private Long teamId;
    private String teamName;
    private Long hostId;
    private List<MemberResponse> memberList;
    private List<TeamPlanResponse> planList;

    public static TeamPlanDetailResponse of(Team team, List<MemberResponse> memberList, List<TeamPlanResponse> planList) {
        return new TeamPlanDetailResponse(team.getId(), team.getName(), team.getHost().getId(), memberList, planList);
    }
}
