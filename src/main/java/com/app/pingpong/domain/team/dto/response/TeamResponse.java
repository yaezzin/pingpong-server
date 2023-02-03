package com.app.pingpong.domain.team.dto.response;

import com.app.pingpong.domain.member.entity.Member;
import com.app.pingpong.domain.member.entity.MemberTeam;
import com.app.pingpong.domain.team.entity.Team;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class TeamResponse {
    private Long teamId;
    private String teamName;
    private List<Long> memberId;

    public static TeamResponse of(Team team) {
        List<Member> members = team.getMembers().stream().map(MemberTeam::getMember).collect(Collectors.toList());
        List<Long> usersList = members.stream().map(Member::getId).collect(Collectors.toList());
        return new TeamResponse(team.getId(), team.getName(), usersList);
    }

    public static TeamResponse of(List<MemberTeam> team) {
        List<Member> members = team.stream().map(MemberTeam::getMember).collect(Collectors.toList());
        List<Long> usersList = members.stream().map(Member::getId).collect(Collectors.toList());
        return new TeamResponse(team.get(0).getTeam().getId(), team.get(0).getTeam().getName(), usersList);
    }
}
