package com.app.pingpong.domain.team.service;

import com.app.pingpong.domain.friend.repository.FriendRepository;
import com.app.pingpong.domain.member.dto.response.MemberResponse;
import com.app.pingpong.domain.member.dto.response.MemberSearchResponse;
import com.app.pingpong.domain.member.entity.Member;
import com.app.pingpong.domain.member.entity.MemberTeam;
import com.app.pingpong.domain.member.repository.MemberRepository;
import com.app.pingpong.domain.member.repository.MemberTeamRepository;
import com.app.pingpong.domain.team.dto.request.TeamRequest;
import com.app.pingpong.domain.team.dto.response.TeamHostResponse;
import com.app.pingpong.domain.team.dto.response.TeamMemberResponse;
import com.app.pingpong.domain.team.dto.response.TeamResponse;
import com.app.pingpong.domain.team.entity.Team;
import com.app.pingpong.domain.team.repository.TeamRepository;
import com.app.pingpong.global.common.Status;
import com.app.pingpong.global.exception.BaseException;
import com.app.pingpong.global.exception.StatusCode;
import com.app.pingpong.global.util.UserFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.app.pingpong.global.common.Status.ACTIVE;
import static com.app.pingpong.global.common.Status.DELETE;
import static com.app.pingpong.global.exception.StatusCode.*;

@RequiredArgsConstructor
@Service
public class TeamService {

    private final MemberRepository memberRepository;
    private final FriendRepository friendRepository;
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

    @Transactional
    public TeamHostResponse updateHost(Long teamId, Long delegatorId) {
        Team team = teamRepository.findActiveTeamById(teamId).orElseThrow(() -> new BaseException(TEAM_NOT_FOUND));
        Member host = memberRepository.findByIdAndStatus(userFacade.getCurrentUser().getId(), ACTIVE).orElseThrow(() -> new BaseException(USER_NOT_FOUND));
        if (team.getHost().getId() != host.getId()) {
            throw new BaseException(INVALID_HOST);
        }
        if (host.getId() == delegatorId) {
            throw new BaseException(ALREADY_TEAM_HOST);
        }
        Member delegator = memberRepository.findByIdAndStatus(delegatorId, ACTIVE).orElseThrow(() -> new BaseException(USER_NOT_FOUND));
        team.setHost(delegator);
        return TeamHostResponse.of(team);
    }

    @Transactional
    public StatusCode delete(Long id) {
        Team team= teamRepository.findById(id).orElseThrow(() -> new BaseException(TEAM_NOT_FOUND));
        if (userFacade.getCurrentUser().getId() != team.getHost().getId()) {
            throw new BaseException(INVALID_HOST);
        }
        team.setStatus(DELETE);

        List<MemberTeam> memberTeam = memberTeamRepository.findAllByTeamId(id);
        for (MemberTeam member : memberTeam) {
            member.setStatus(DELETE);
        }
        return SUCCESS;
    }

    public List<TeamMemberResponse> getTeamMembers(Long id) {
        List<MemberTeam> memberTeam = memberTeamRepository.findAllByTeamId(id);
        Team team = teamRepository.findActiveTeamById(id).orElseThrow(() -> new BaseException(TEAM_NOT_FOUND));
        List<Member> members = getMembersFromUserTeams(memberTeam);
        Long hostId = team.getHost().getId();

        List<TeamMemberResponse> list = new ArrayList<>();
        for (Member findMember : members) {
            boolean isFriend = friendRepository.isFriend(hostId, findMember.getId());
            list.add(TeamMemberResponse.builder()
                    .userId(findMember.getId())
                    .nickname(findMember.getNickname())
                    .profileImage(findMember.getProfileImage())
                    .hostId(hostId)
                    .isFriend(isFriend)
                    .build());
        }
        return list;
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

    private List<Member> getMembersFromUserTeams(List<MemberTeam> memberTeams) {
        return memberTeams.stream().map(MemberTeam::getMember).collect(Collectors.toList());
    }
}
