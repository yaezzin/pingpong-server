package com.app.pingpong.factory;

import com.app.pingpong.domain.member.entity.Member;
import com.app.pingpong.domain.team.entity.Team;

public class TeamFactory {

    public static Team createTeam(Member host) {
        Team team = new Team("team");
        team.setHost(host);
        return team;
    }
}
