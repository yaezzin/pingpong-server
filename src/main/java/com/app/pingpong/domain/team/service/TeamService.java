package com.app.pingpong.domain.team.service;

import com.app.pingpong.domain.member.dto.response.MemberResponse;
import com.app.pingpong.domain.member.entity.Member;
import com.app.pingpong.domain.member.entity.MemberTeam;
import com.app.pingpong.domain.member.repository.MemberRepository;
import com.app.pingpong.domain.member.repository.MemberTeamRepository;
import com.app.pingpong.domain.team.dto.request.TeamRequest;
import com.app.pingpong.domain.team.dto.response.TeamResponse;
import com.app.pingpong.domain.team.entity.Team;
import com.app.pingpong.domain.team.repository.TeamRepository;
import com.app.pingpong.global.common.Status;
import com.app.pingpong.global.exception.BaseException;
import com.app.pingpong.global.util.UserFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.app.pingpong.global.exception.StatusCode.*;

@RequiredArgsConstructor
@Service
public class TeamService {

    private final MemberRepository memberRepository;
    private final TeamRepository teamRepository;
    private final MemberTeamRepository memberTeamRepository;
    private final UserFacade userFacade;

    @Transactional
    public TeamResponse create(TeamRequest request) {
        Member loginMember = userFacade.getCurrentUser();
        if (request.getMemberId().size() > 10 || request.getMemberId().size() < 1) {
            throw new BaseException(INVALID_TEAM_MEMBER_SIZE);
        }
        if (teamRepository.findByHostId(loginMember.getId()).size() > 6) {
            throw new BaseException(EXCEED_HOST_TEAM_SIZE);
        }

        Team newTeam = teamRepository.save(request.toEntity());
        newTeam.setHost(loginMember);
        newTeam.setStatus(Status.ACTIVE);
        setTeamToHost(newTeam, loginMember);
        setTeamToUsers(newTeam, loginMember, request);

        return TeamResponse.of(memberTeamRepository.findAllByTeamId(newTeam.getId()));
    }

    private void setTeamToHost(Team team, Member loginMember) {
        MemberTeam memberTeam = new MemberTeam();
        memberTeam.setTeam(team);
        memberTeam.setMember(loginMember);
        memberTeam.setStatus(Status.WAIT);
        memberTeamRepository.save(memberTeam);
    }

    private void setTeamToUsers(Team newTeam, Member currentUser, TeamRequest request) {
        for (Long memberId : request.getMemberId()) {
            MemberTeam memberTeam = new MemberTeam();
            memberTeam.setTeam(newTeam);
            memberTeam.setStatus(Status.WAIT);
            // 만약 방장이 들어가게 되면 오류 뜨게
            if (memberId == currentUser.getId()) {
                throw new BaseException(INVALID_TEAM_MEMBER);
            }
            Member member = memberRepository.findById(memberId).orElseThrow(() -> new BaseException(USER_NOT_FOUND));
            memberTeam.setMember(member);
            memberTeamRepository.save(memberTeam);
        }
    }
}
