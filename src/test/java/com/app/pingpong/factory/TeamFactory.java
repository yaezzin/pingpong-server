package com.app.pingpong.factory;

import com.app.pingpong.domain.member.entity.Member;
import com.app.pingpong.domain.team.entity.Team;

import static com.app.pingpong.global.common.status.Status.ACTIVE;

public class TeamFactory {

    public static Team createTeam(Member host) {
        Team team = new Team("team");
        team.setHost(host);
        team.setStatus(ACTIVE);
        return team;
    }
}
