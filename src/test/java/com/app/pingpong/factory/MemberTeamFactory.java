package com.app.pingpong.factory;

import com.app.pingpong.domain.member.entity.Member;
import com.app.pingpong.domain.member.entity.MemberTeam;
import com.app.pingpong.domain.team.entity.Team;

import static com.app.pingpong.global.common.status.Status.ACTIVE;
import static com.app.pingpong.global.common.status.Status.WAIT;

public class MemberTeamFactory {

    public static MemberTeam createMemberTeam(Member member, Team team) {
        MemberTeam memberTeam = new MemberTeam();
        memberTeam.setMember(member);
        memberTeam.setTeam(team);
        memberTeam.setStatus(ACTIVE);
        return memberTeam;
    }

    public static MemberTeam createWaitMemberTeam(Member member, Team team) {
        MemberTeam memberTeam = new MemberTeam();
        memberTeam.setMember(member);
        memberTeam.setTeam(team);
        memberTeam.setStatus(WAIT);
        return memberTeam;
    }
}
