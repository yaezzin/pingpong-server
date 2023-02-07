package com.app.pingpong.domain.team.dto.response;

import com.app.pingpong.domain.member.entity.Authority;
import com.app.pingpong.domain.member.entity.Member;
import com.app.pingpong.domain.member.entity.MemberTeam;
import com.app.pingpong.domain.team.entity.Team;
import com.app.pingpong.global.common.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.stream.Collectors;

import static com.app.pingpong.global.common.Status.ACTIVE;

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
