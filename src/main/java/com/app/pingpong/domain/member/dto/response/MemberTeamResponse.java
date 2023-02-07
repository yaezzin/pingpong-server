package com.app.pingpong.domain.member.dto.response;

import com.app.pingpong.domain.team.entity.Team;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberTeamResponse {
    private Long teamId;
    private String teamName;
    private Long hostId;
    private List<MemberResponse> members;

    public static MemberTeamResponse of(Team team, List<MemberResponse> member) {
        return new MemberTeamResponse(team.getId(), team.getName(), team.getHost().getId(), member);
    }
}
