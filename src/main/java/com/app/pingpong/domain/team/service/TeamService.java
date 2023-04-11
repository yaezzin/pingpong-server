package com.app.pingpong.domain.team.service;

import com.app.pingpong.domain.friend.repository.FriendFactory;
import com.app.pingpong.domain.friend.repository.FriendRepository;
import com.app.pingpong.domain.member.dto.response.MemberResponse;
import com.app.pingpong.domain.member.entity.Member;
import com.app.pingpong.domain.member.entity.MemberTeam;
import com.app.pingpong.domain.member.repository.MemberRepository;
import com.app.pingpong.domain.member.repository.MemberTeamRepository;
import com.app.pingpong.domain.team.dto.request.TeamAchieveRequest;
import com.app.pingpong.domain.team.dto.request.TeamPlanPassRequest;
import com.app.pingpong.domain.team.dto.request.TeamPlanRequest;
import com.app.pingpong.domain.team.dto.request.TeamRequest;
import com.app.pingpong.domain.team.dto.response.*;
import com.app.pingpong.domain.team.entity.Plan;
import com.app.pingpong.domain.team.entity.Team;
import com.app.pingpong.domain.team.repository.PlanRepository;
import com.app.pingpong.domain.team.repository.TeamRepository;
import com.app.pingpong.global.common.Status;
import com.app.pingpong.global.exception.BaseException;
import com.app.pingpong.global.exception.StatusCode;
import com.app.pingpong.global.util.MemberFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.app.pingpong.global.common.Status.*;
import static com.app.pingpong.global.exception.StatusCode.*;

@RequiredArgsConstructor
@Service
public class TeamService {

    private final MemberRepository memberRepository;
    private final FriendFactory friendFactory;
    private final TeamRepository teamRepository;
    private final PlanRepository planRepository;
    private final MemberTeamRepository memberTeamRepository;
    private final MemberFacade userFacade;

    @Transactional
    public TeamResponse create(TeamRequest request) {
        Member loginMember = userFacade.getCurrentMember();
        checkTeam(loginMember, request);

        Team newTeam = teamRepository.save(request.toEntity());
        newTeam.setHost(loginMember);
        newTeam.setStatus(Status.ACTIVE);
        setTeamToHost(newTeam, loginMember);
        setTeamToUsers(newTeam, loginMember, request);

        return TeamResponse.of(memberTeamRepository.findAllByTeamId(newTeam.getId()));
    }

    @Transactional
    public StatusCode delete(Long id) {
        Team team= teamRepository.findByIdAndStatus(id, ACTIVE).orElseThrow(() -> new BaseException(TEAM_NOT_FOUND));
        if (userFacade.getCurrentMember().getId() != team.getHost().getId()) {
            throw new BaseException(INVALID_HOST);
        }
        team.setStatus(DELETE);
        List<MemberTeam> memberTeam = memberTeamRepository.findAllByTeamId(id);
        for (MemberTeam member : memberTeam) {
            member.setStatus(DELETE);
        }
        return SUCCESS;
    }

    @Transactional
    public TeamHostResponse updateHost(Long teamId, Long delegatorId) {
        Team team = teamRepository.findByIdAndStatus(teamId, ACTIVE).orElseThrow(() -> new BaseException(TEAM_NOT_FOUND));
        Member host = memberRepository.findByIdAndStatus(userFacade.getCurrentMember().getId(), ACTIVE).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));
        Member delegator = memberRepository.findByIdAndStatus(delegatorId, ACTIVE).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));
        checkHostForDelegate(team, host, delegatorId);
        team.setHost(delegator);
        return TeamHostResponse.of(team, getTeamMemberStatus(team));
    }

    @Transactional
    public TeamHostResponse emit(Long teamId, Long emitterId) {
        Team team = teamRepository.findByIdAndStatus(teamId, ACTIVE).orElseThrow(() -> new BaseException(TEAM_NOT_FOUND));
        Member host = memberRepository.findByIdAndStatus(userFacade.getCurrentMember().getId(), ACTIVE).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));
        checkHostForEmit(team, host, emitterId);
        memberRepository.findByIdAndStatus(emitterId, ACTIVE).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));
        MemberTeam memberTeam = memberTeamRepository.findByTeamIdAndMemberIdAndStatus(teamId, emitterId, ACTIVE)
                .orElseThrow(() -> new BaseException(USER_ALREADY_EMIT));
        memberTeam.setStatus(DELETE);
        return TeamHostResponse.of(team, getTeamMemberStatus(team));
    }

    @Transactional
    public List<TeamMemberResponse> getTeamMembers(Long teamId) {
        List<MemberTeam> memberTeam = memberTeamRepository.findAllByTeamId(teamId);
        Team team = teamRepository.findByIdAndStatus(teamId, ACTIVE).orElseThrow(() -> new BaseException(TEAM_NOT_FOUND));
        List<Member> members = getMembersFromUserTeams(memberTeam);
        Long hostId = team.getHost().getId();

        List<TeamMemberResponse> list = new ArrayList<>();
        for (Member findMember : members) {
            boolean isFriend = friendFactory.isFriend(hostId, findMember.getId());
            MemberTeam isStatus = memberTeamRepository.findByTeamIdAndMemberId(team.getId(), findMember.getId()).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND_IN_TEAM));
            list.add(TeamMemberResponse.builder()
                    .memberId(findMember.getId())
                    .nickname(findMember.getNickname())
                    .profileImage(findMember.getProfileImage())
                    .hostId(hostId)
                    .isFriend(isFriend)
                    .status(isStatus.getStatus())
                    .build());
        }
        return list;
    }

    @Transactional
    public StatusCode accept(Long teamId) {
        Team team = teamRepository.findByIdAndStatus(teamId, ACTIVE).orElseThrow(() -> new BaseException(TEAM_NOT_FOUND));
        MemberTeam memberTeam = memberTeamRepository.findByTeamIdAndMemberIdAndStatus(teamId, userFacade.getCurrentMember().getId(), WAIT)
                .orElseThrow(() -> new BaseException(TEAM_INVITATION_NOT_FOUND));
        memberTeamRepository.findByTeamIdAndMemberIdAndStatus(teamId, userFacade.getCurrentMember().getId(), ACTIVE)
                .orElseThrow(() -> new BaseException(ALREADY_ACCEPT_TEAM_INVITATION));
        memberTeam.setStatus(ACTIVE);
        memberTeam.setParticipatedAt(new Date());
        return SUCCESS_ACCEPT_TEAM_INVITATION;
    }

    @Transactional
    public StatusCode refuse(Long teamId) {
        Team team = teamRepository.findByIdAndStatus(teamId, ACTIVE).orElseThrow(() -> new BaseException(TEAM_NOT_FOUND));
        MemberTeam memberTeam = memberTeamRepository.findByTeamIdAndMemberIdAndStatus(teamId, userFacade.getCurrentMember().getId(), WAIT)
                .orElseThrow(() -> new BaseException(TEAM_INVITATION_NOT_FOUND));
        memberTeam.setStatus(DELETE);
        return SUCCESS_REFUSE_TEAM_INVITATION;
    }

    @Transactional
    public TeamPlanResponse createPlan(Long teamId, TeamPlanRequest request) {
        // 1. 할 일을 담당할 사람이 존재하는지 확인 & 팀에 있는지 확인
        Member manager = memberRepository.findByIdAndStatus(request.getManagerId(), ACTIVE).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));
        memberTeamRepository.findByTeamIdAndMemberId(teamId, manager.getId()).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND_IN_TEAM));

        // 2. 플랜을 생성하려는 멤버가 팀에 속해있는지 확인
        Member maker = memberRepository.findByIdAndStatus(userFacade.getCurrentMember().getId(), ACTIVE).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));;
        memberTeamRepository.findByTeamIdAndMemberId(teamId, manager.getId()).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND_IN_TEAM));

        Team team = teamRepository.findByIdAndStatus(teamId, ACTIVE).orElseThrow(() -> new BaseException(TEAM_NOT_FOUND));
        Plan plan = request.toEntity();
        plan.setManager(manager);
        plan.setTeam(team);
        plan.setStatus(ACTIVE);
        plan.setAchievement(INCOMPLETE);
        return TeamPlanResponse.of(planRepository.save(plan));
    }

    @Transactional
    public TeamPlanResponse deletePlan(Long teamId, Long planId) {
        Member member = memberRepository.findByIdAndStatus(userFacade.getCurrentMember().getId(), ACTIVE).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));;
        memberTeamRepository.findByTeamIdAndMemberId(teamId, member.getId()).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND_IN_TEAM));
        Plan plan = planRepository.findById(planId).orElseThrow(() -> new BaseException(PLAN_NOT_FOUND));
        plan.setStatus(DELETE);
        return TeamPlanResponse.of(plan);
    }

    @Transactional
    public TeamPlanResponse passPlan(Long teamId, TeamPlanPassRequest request) {
        Member currentMember = memberRepository.findByIdAndStatus(userFacade.getCurrentMember().getId(), ACTIVE).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));
        memberTeamRepository.findByTeamIdAndMemberId(teamId, currentMember.getId()).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND_IN_TEAM));

        Member mandator = memberRepository.findByIdAndStatus(request.getMandaterId(), ACTIVE).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));
        memberTeamRepository.findByTeamIdAndMemberId(teamId, mandator.getId()).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND_IN_TEAM));

        Plan plan = planRepository.findById(request.getPlanId()).orElseThrow(() -> new BaseException(PLAN_NOT_FOUND));
        if (plan.getManager().getId() != currentMember.getId()) {
            throw new BaseException(INVALID_MANAGER);
        }
        if (plan.getStatus() != ACTIVE) {
            throw new BaseException(INVALID_PLAN);
        }

        plan.setManager(mandator);
        return TeamPlanResponse.of(plan);
    }

    @Transactional
    public StatusCode completePlan(Long teamId, Long planId) {
        Team team = teamRepository.findByIdAndStatus(teamId, ACTIVE).orElseThrow(() -> new BaseException(TEAM_NOT_FOUND));
        Member currentMember = memberRepository.findByIdAndStatus(userFacade.getCurrentMember().getId(), ACTIVE).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));
        Plan plan = planRepository.findByIdAndTeamIdAndStatus(planId, teamId, ACTIVE).orElseThrow(() -> new BaseException(PLAN_NOT_FOUND));
        if (plan.getManager().getId() != currentMember.getId()) {
            throw new BaseException(INVALID_COMPLETE_PLAN);
        }
        plan.setAchievement(COMPLETE);
        return SUCCESS_COMPLETE_PLAN;
    }

    @Transactional
    public StatusCode incompletePlan(Long teamId, Long planId) {
        Team team = teamRepository.findByIdAndStatus(teamId, ACTIVE).orElseThrow(() -> new BaseException(TEAM_NOT_FOUND));
        Member currentMember = memberRepository.findByIdAndStatus(userFacade.getCurrentMember().getId(), ACTIVE).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));
        Plan plan = planRepository.findByIdAndTeamIdAndStatus(planId, teamId, ACTIVE).orElseThrow(() -> new BaseException(PLAN_NOT_FOUND));
        if (plan.getAchievement() == INCOMPLETE) {
            throw new BaseException(ALREADY_INCOMPLETE_PLAN);
        }
        plan.setAchievement(INCOMPLETE);
        return SUCCESS_INCOMPLETE_PLAN;
    }

    @Transactional(readOnly = true)
    public TeamPlanDetailResponse getTeamCalendarByDate(Long teamId, LocalDate date) {
        Team team = teamRepository.findByIdAndStatus(teamId, ACTIVE).orElseThrow(() -> new BaseException(TEAM_NOT_FOUND));

        List<MemberTeam> memberTeam = memberTeamRepository.findALLByTeamIdAndStatus(teamId, ACTIVE);
        List<Member> members = memberTeam.stream().map(MemberTeam::getMember).collect(Collectors.toList());
        List<Long> memberIdList = members.stream().map(Member::getId).collect(Collectors.toList());

        List<MemberResponse> memberList = new ArrayList<>();
        for (Long id : memberIdList) {
            Member member = memberRepository.findByIdAndStatus(id, ACTIVE).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));
            memberList.add(MemberResponse.builder()
                    .memberId(member.getId())
                    .nickname(member.getNickname())
                    .profileImage(member.getProfileImage())
                    .build()
            );
        }

        List<Plan> plans = planRepository.findAllByTeamIdAndDateAndStatus(teamId, date, ACTIVE);
        List<TeamPlanResponse> planList = new ArrayList<>();
        for (Plan plan : plans) {
            planList.add(TeamPlanResponse.builder()
                    .planId(plan.getId())
                    .managerId(plan.getManager().getId())
                    .title(plan.getTitle())
                    .date(plan.getDate())
                    .status(plan.getStatus())
                    .achievement(plan.getAchievement())
                    .build()
            );
        }
        return TeamPlanDetailResponse.of(team, memberList, planList);
    }

    @Transactional
    public List<TeamAchieveResponse> getTeamAchievementRate(Long teamId, TeamAchieveRequest request) {
        // 팀에 있는 모든 일정을 월 별 날짜별로 가져옴
        List<Plan> plans = planRepository.findAllByTeamIdAndStatusAndDateBetween(teamId, ACTIVE, request.getStartDate(), request.getEndDate());
        List<LocalDate> dateList = plans.stream().map(Plan::getDate).collect(Collectors.toList());

        int complete = 0;
        int incomplete = 0;
        double achievement = 0;
        List<TeamAchieveResponse> response = new ArrayList<>();
        for (LocalDate date :dateList) {
            List<Plan> plan = planRepository.findAllByTeamIdAndStatusAndDate(teamId, ACTIVE, date);
            List<Status> achieves = plan.stream().map(Plan::getAchievement).collect(Collectors.toList());
            for (Status achieve : achieves) {
                if (achieve == COMPLETE) {
                    complete += 1;
                } else {
                    incomplete += 1;
                }
            }
            achievement = ((double)(complete) / (double) (complete + incomplete) * 100.0);
            response.add(new TeamAchieveResponse(date, achievement));
        }
        return response;
    }

    @Transactional(readOnly = true)
    public List<TeamPlanResponse> getTrash(Long teamId) {
        Team team = teamRepository.findByIdAndStatus(teamId, ACTIVE).orElseThrow(() -> new BaseException(TEAM_NOT_FOUND));

        Member currentMember = memberRepository.findByIdAndStatus(userFacade.getCurrentMember().getId(), ACTIVE).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));
        memberTeamRepository.findByTeamIdAndMemberId(teamId, currentMember.getId()).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND_IN_TEAM));

        List<Plan> plansInTrash = planRepository.findAllByTeamIdAndStatusOrderByWastedTimeDesc(teamId, DELETE);

        return TeamPlanResponse.of(plansInTrash);
    }

    @Transactional
    public StatusCode deleteAllTrash(Long teamId) {
        Team team = teamRepository.findByIdAndStatus(teamId, ACTIVE).orElseThrow(() -> new BaseException(TEAM_NOT_FOUND));

        Member currentMember = memberRepository.findByIdAndStatus(userFacade.getCurrentMember().getId(), ACTIVE).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));
        memberTeamRepository.findByTeamIdAndMemberId(teamId, currentMember.getId()).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND_IN_TEAM));

        if (currentMember.getId() != team.getHost().getId()) {
            throw new BaseException(INVALID_HOST);
        }

        List<Plan> plansInTrash = planRepository.findAllByTeamIdAndStatusOrderByWastedTimeDesc(teamId, DELETE);
        for (Plan p : plansInTrash) {
            p.setStatus(PERMANENT);
        }

        return SUCCESS_DELETE_ALL_TRASH;
    }

    @Transactional
    public StatusCode deleteTrash(Long teamId, Long planId) {
        Team team = teamRepository.findByIdAndStatus(teamId, ACTIVE).orElseThrow(() -> new BaseException(TEAM_NOT_FOUND));

        Member currentMember = memberRepository.findByIdAndStatus(userFacade.getCurrentMember().getId(), ACTIVE).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));
        memberTeamRepository.findByTeamIdAndMemberId(teamId, currentMember.getId()).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND_IN_TEAM));

        if (currentMember.getId() != team.getHost().getId()) {
            throw new BaseException(INVALID_HOST);
        }

        Plan plan = planRepository.findById(planId).orElseThrow(() -> new BaseException(PLAN_NOT_FOUND));
        plan.setStatus(DELETE);
        return SUCCESS_DELETE_TRASH;
    }

    @Transactional
    public StatusCode recoverTrash(Long teamId, Long planId) {
        Team team = teamRepository.findByIdAndStatus(teamId, ACTIVE).orElseThrow(() -> new BaseException(TEAM_NOT_FOUND));

        Member currentMember = memberRepository.findByIdAndStatus(userFacade.getCurrentMember().getId(), ACTIVE).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));
        memberTeamRepository.findByTeamIdAndMemberId(teamId, currentMember.getId()).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND_IN_TEAM));

        Plan plan = planRepository.findByIdAndStatus(planId, DELETE).orElseThrow(() -> new BaseException(PLAN_NOT_FOUND));
        plan.setStatus(ACTIVE);

        return SUCCESS_RECOVER_TRASH;
    }

    private void checkTeam(Member loginMember, TeamRequest request) {
        if (teamRepository.findByHostId(loginMember.getId()).size() > 6) {
            throw new BaseException(EXCEED_HOST_TEAM_SIZE);
        }
        if (request.getMemberId().size() > 10 || request.getMemberId().size() < 1) {
            throw new BaseException(INVALID_TEAM_MEMBER_SIZE);
        }
        for (Long id : request.getMemberId()) {
            memberRepository.findByIdAndStatus(id, ACTIVE).orElseThrow(() -> new BaseException(INVALID_INVITER));
        }
    }

    private void setTeamToHost(Team team, Member loginMember) {
        MemberTeam memberTeam = new MemberTeam();
        memberTeam.setTeam(team);
        memberTeam.setMember(loginMember);
        memberTeam.setStatus(Status.ACTIVE);
        memberTeamRepository.save(memberTeam);
    }

    private void setTeamToUsers(Team newTeam, Member currentUser, TeamRequest request) {
        for (Long memberId : request.getMemberId()) {
            MemberTeam memberTeam = new MemberTeam();
            memberTeam.setTeam(newTeam);
            memberTeam.setStatus(WAIT);
            if (memberId == currentUser.getId()) {
                throw new BaseException(INVALID_TEAM_MEMBER);
            }
            Member member = memberRepository.findById(memberId).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));
            memberTeam.setMember(member);
            memberTeamRepository.save(memberTeam);
        }
    }

    private void checkHostForEmit(Team team, Member host, Long emitterId) {
        if (team.getHost().getId() != host.getId()) {
            throw new BaseException(INVALID_HOST);
        }
        if (host.getId() == emitterId) {
            throw new BaseException(INVALID_EMITTER);
        }
    }

    private void checkHostForDelegate(Team team, Member host, Long delegatorId) {
        if (team.getHost().getId() != host.getId()) {
            throw new BaseException(INVALID_HOST);
        }
        if (host.getId() == delegatorId) {
            throw new BaseException(ALREADY_TEAM_HOST);
        }
    }

    private List<TeamCompactResponse> getTeamMemberStatus(Team team) {
        List<TeamCompactResponse> list = new ArrayList<>();
        List<Member> members = team.getMembers().stream().map(MemberTeam::getMember).collect(Collectors.toList());
        for (Member m : members) {
            MemberTeam m1 = memberTeamRepository.findByTeamIdAndMemberId(team.getId(), m.getId()).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND_IN_TEAM));
            list.add(TeamCompactResponse.builder()
                    .memberId(m.getId())
                    .status(m1.getStatus())
                    .build());
        }
        return list;
    }

    private List<Member> getMembersFromUserTeams(List<MemberTeam> memberTeams) {
        return memberTeams.stream().map(MemberTeam::getMember).collect(Collectors.toList());
    }


}

