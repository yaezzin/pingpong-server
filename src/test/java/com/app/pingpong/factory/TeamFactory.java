package com.app.pingpong.factory;

import com.app.pingpong.domain.member.entity.Member;
import com.app.pingpong.domain.team.entity.Team;

import java.util.ArrayList;
import java.util.List;

import static com.app.pingpong.global.common.status.Status.ACTIVE;
import static com.app.pingpong.global.common.status.Status.DELETE;

public class TeamFactory {

    public static Team createTeam(Member host) {
        Team team = new Team("team");
        team.setHost(host);
        team.setStatus(ACTIVE);
        return team;
    }

    public static Team createDeleteTeam(Member host) {
        Team team = new Team("team");
        team.setHost(host);
        team.setStatus(DELETE);
        return team;
    }

    public static List<Team> createTeamList(Member host, int count) {
        List<Team> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            Team team = new Team("team");
            team.setHost(host);
            team.setStatus(ACTIVE);
            list.add(team);
        }
        return list;
    }
}
