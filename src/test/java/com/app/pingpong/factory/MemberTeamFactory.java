package com.app.pingpong.factory;

import com.app.pingpong.domain.member.entity.Member;
import com.app.pingpong.domain.member.entity.MemberTeam;
import com.app.pingpong.domain.team.entity.Team;

import static com.app.pingpong.global.common.status.Status.ACTIVE;

public class MemberTeamFactory {

    public static MemberTeam createMemberTeam(Member member, Team team) {
        MemberTeam memberTeam = new MemberTeam();
        memberTeam.setMember(member);
        memberTeam.setTeam(team);
        memberTeam.setStatus(ACTIVE);
        return memberTeam;
    }
}
