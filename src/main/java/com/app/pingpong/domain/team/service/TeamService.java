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
import java.util.Optional;
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
    private final MemberFacade memberFacade;

    @Transactional
    public TeamResponse create(TeamRequest request) {
        Member host = memberFacade.getCurrentMember();
        checkTeam(host, request);

        Team team = teamRepository.save(request.toEntity());

        setTeam(team, host);
        setTeamToHost(team, host);
        setTeamToMembers(team, host, request);

        return TeamResponse.of(memberTeamRepository.findAllByTeamId(team.getId()));
    }

    @Transactional
    public StatusCode delete(Long id) {
        Team team = teamRepository.findByIdAndStatus(id, ACTIVE).orElseThrow(() -> new BaseException(TEAM_NOT_FOUND));
        checkHost(team.getHost());
        deleteTeamAndMembers(team);
        return SUCCESS_DELETE_TEAM;
    }

    @Transactional
    public TeamHostResponse updateHost(Long teamId, Long delegatorId) {
        Team team = teamRepository.findByIdAndStatus(teamId, ACTIVE).orElseThrow(() -> new BaseException(TEAM_NOT_FOUND));
        Member host = memberRepository.findByIdAndStatus(memberFacade.getCurrentMember().getId(), ACTIVE).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));
        Member delegator = memberRepository.findByIdAndStatus(delegatorId, ACTIVE).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));
        checkHostForDelegate(team, host, delegator);
        return TeamHostResponse.of(team, getTeamMemberStatus(team));
    }

    @Transactional
    public TeamHostResponse emit(Long teamId, Long emitterId) {
        Team team = teamRepository.findByIdAndStatus(teamId, ACTIVE).orElseThrow(() -> new BaseException(TEAM_NOT_FOUND));
        Member host = memberRepository.findByIdAndStatus(memberFacade.getCurrentMember().getId(), ACTIVE).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));
        checkHostForEmit(team, host, emitterId);
        memberRepository.findByIdAndStatus(emitterId, ACTIVE).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));
        MemberTeam memberTeam = memberTeamRepository.findByTeamIdAndMemberIdAndStatus(teamId, emitterId, ACTIVE).orElseThrow(() -> new BaseException(USER_ALREADY_EMIT));
        memberTeam.setStatus(DELETE);
        return TeamHostResponse.of(team, getTeamMemberStatus(team));
    }

    /* host 관점 */
    @Transactional
    public List<TeamMemberResponse> getTeamMembers(Long teamId) {
        List<MemberTeam> memberTeam = memberTeamRepository.findAllByTeamId(teamId);
        Team team = teamRepository.findByIdAndStatus(teamId, ACTIVE).orElseThrow(() -> new BaseException(TEAM_NOT_FOUND));
        List<Member> members = getMembersFromMemberTeams(memberTeam);
        return buildTeamMemberResponseList(members, team);
    }

    @Transactional
    public StatusCode accept(Long teamId) {
        checkTeamExists(teamId);
        checkTeamInvitationAlreadyExists(teamId);
        inviteMemberToTeam(teamId);
        return SUCCESS_ACCEPT_TEAM_INVITATION;
    }

    @Transactional
    public StatusCode refuse(Long teamId) {
        checkTeamExists(teamId);
        refuseTeamInvitation(teamId);
        return SUCCESS_REFUSE_TEAM_INVITATION;
    }

    @Transactional
    public TeamPlanResponse createPlan(Long teamId, TeamPlanRequest request) {
        Member manager = checkManagerExistsAndMembership(teamId, request); // manager != maker
        checkMakerExistsAndMemberShip(teamId);
        Plan plan = createPlan(teamId, manager, request);
        return TeamPlanResponse.of(planRepository.save(plan));
    }

    @Transactional
    public TeamPlanResponse deletePlan(Long teamId, Long planId) {
        Member member = memberRepository.findByIdAndStatus(memberFacade.getCurrentMember().getId(), ACTIVE).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));;
        memberTeamRepository.findByTeamIdAndMemberId(teamId, member.getId()).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND_IN_TEAM));
        Plan plan = planRepository.findById(planId).orElseThrow(() -> new BaseException(PLAN_NOT_FOUND));
        plan.setStatus(DELETE);
        return TeamPlanResponse.of(plan);
    }

    @Transactional
    public TeamPlanResponse passPlan(Long teamId, TeamPlanPassRequest request) {
       Member currentMember = checkCurrentMemberExistsInTeam(teamId);
       Member mandator = checkMandatorInTeam(teamId, request);
       Plan plan = passPlan(request, mandator);
       return TeamPlanResponse.of(plan);
    }

    @Transactional
    public StatusCode completePlan(Long teamId, Long planId) {
        checkMemberInTeam(teamId);
        complete(teamId, planId);
        return SUCCESS_COMPLETE_PLAN;
    }

    @Transactional
    public StatusCode incompletePlan(Long teamId, Long planId) {
        checkMemberInTeam(teamId);
        incomplete(teamId, planId);
        return SUCCESS_INCOMPLETE_PLAN;
    }

    @Transactional(readOnly = true)
    public TeamPlanDetailResponse getTeamCalendarByDate(Long teamId, LocalDate date) {
        Team team = checkMemberInTeam(teamId);
        List<MemberResponse> memberList = getMembersByTeamId(teamId);
        List<TeamPlanResponse> planList = getPlansByDate(teamId, date);
        return TeamPlanDetailResponse.of(team, memberList, planList);
    }

    @Transactional
    public List<TeamAchieveResponse> getTeamAchievementRate(Long teamId, TeamAchieveRequest request) {
        List<LocalDate> dateList = planRepository.findAllByTeamIdAndStatusAndDateBetween(teamId, ACTIVE, request.getStartDate(), request.getEndDate())
                .stream()
                .map(Plan::getDate)
                .distinct()
                .collect(Collectors.toList());

        List<TeamAchieveResponse> response = dateList.stream().map(date -> {
            List<Plan> plans = planRepository.findAllByTeamIdAndStatusAndDate(teamId, ACTIVE, date);
            long complete = plans.stream().filter(plan -> plan.getAchievement() == COMPLETE).count();
            System.out.println("===== complete " + complete);
            long incomplete = plans.size() - complete;
            double achievement = (complete + incomplete) == 0 ? 0 : ((double) complete / (double) (complete + incomplete) * 100.0);
            return new TeamAchieveResponse(date, achievement);
        }).collect(Collectors.toList());

        return response;
    }

    @Transactional(readOnly = true)
    public List<TeamPlanResponse> getTrash(Long teamId, Long loginMemberId) {
        Team team = teamRepository.findByIdAndStatus(teamId, ACTIVE).orElseThrow(() -> new BaseException(TEAM_NOT_FOUND));
        memberTeamRepository.findByTeamIdAndMemberId(teamId, loginMemberId).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND_IN_TEAM));

        List<Plan> plansInTrash;
        if (loginMemberId == team.getHost().getId()) {
            plansInTrash = planRepository.findAllByTeamIdAndStatusOrderByWastedTimeDesc(teamId, DELETE);
        } else {
            plansInTrash = planRepository.findAllByManagerIdAndTeamIdAndStatusOrderByWastedTimeDesc(loginMemberId, teamId, DELETE);
        }
        return TeamPlanResponse.of(plansInTrash);
    }

    @Transactional
    public StatusCode deleteAllTrash(Long teamId, Long loginMemberId) {
        Team team = teamRepository.findByIdAndStatus(teamId, ACTIVE).orElseThrow(() -> new BaseException(TEAM_NOT_FOUND));
        memberTeamRepository.findByTeamIdAndMemberId(teamId, loginMemberId).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND_IN_TEAM));
        isHost(loginMemberId, team);

        List<Plan> plansInTrash = planRepository.findAllByTeamIdAndStatusOrderByWastedTimeDesc(teamId, DELETE);
        for (Plan p : plansInTrash) {
            p.setStatus(PERMANENT);
        }
        return SUCCESS_DELETE_ALL_TRASH;
    }

    private void isHost(Long loginMemberId, Team team) {
        if (loginMemberId != team.getHost().getId()) {
            throw new BaseException(INVALID_HOST);
        }
    }

    @Transactional
    public StatusCode deleteTrash(Long teamId, Long planId) {
        Team team = teamRepository.findByIdAndStatus(teamId, ACTIVE).orElseThrow(() -> new BaseException(TEAM_NOT_FOUND));

        Member currentMember = memberRepository.findByIdAndStatus(memberFacade.getCurrentMember().getId(), ACTIVE).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));
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

        Member currentMember = memberRepository.findByIdAndStatus(memberFacade.getCurrentMember().getId(), ACTIVE).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));
        memberTeamRepository.findByTeamIdAndMemberId(teamId, currentMember.getId()).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND_IN_TEAM));

        Plan plan = planRepository.findByIdAndStatus(planId, DELETE).orElseThrow(() -> new BaseException(PLAN_NOT_FOUND));
        plan.setStatus(ACTIVE);

        return SUCCESS_RECOVER_TRASH;
    }

    private void setTeam(Team team, Member host) {
        team.setHost(host);
        team.setStatus(Status.ACTIVE);
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

    private void setTeamToMembers(Team newTeam, Member host, TeamRequest request) {
        request.getMemberId().stream()
                .map(memberRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(member -> {
                    if (member.getId().equals(host.getId())) {
                        throw new BaseException(INVALID_TEAM_MEMBER);
                    }
                    MemberTeam memberTeam = new MemberTeam();
                    memberTeam.setTeam(newTeam);
                    memberTeam.setMember(member);
                    memberTeam.setStatus(WAIT);
                    memberTeamRepository.save(memberTeam);
                });
    }

    private void checkHost(Member host) {
        if (memberFacade.getCurrentMember().getId() != host.getId()) {
            throw new BaseException(INVALID_HOST);
        }
    }

    private void deleteTeamAndMembers(Team team) {
        team.setStatus(DELETE);
        List<MemberTeam> memberTeams = memberTeamRepository.findAllByTeamId(team.getId());
        memberTeams.forEach(memberTeam -> memberTeam.setStatus(DELETE));
    }

    private void checkHostForDelegate(Team team, Member host, Member delegator) {
        if (team.getHost().getId() != host.getId()) {
            throw new BaseException(INVALID_HOST);
        }
        if (host.getId() == delegator.getId()) {
            throw new BaseException(ALREADY_TEAM_HOST);
        }
        team.setHost(delegator);
    }

    private void checkHostForEmit(Team team, Member host, Long emitterId) {
        if (team.getHost().getId() != host.getId()) {
            throw new BaseException(INVALID_HOST);
        }
        if (host.getId() == emitterId) {
            throw new BaseException(INVALID_EMITTER);
        }
    }

    private List<Member> getMembersFromMemberTeams(List<MemberTeam> memberTeams) {
        return memberTeams.stream()
                .map(MemberTeam::getMember)
                .collect(Collectors.toList());
    }

    private List<TeamMemberResponse> buildTeamMemberResponseList(List<Member> members, Team team) {
        List<TeamMemberResponse> list = new ArrayList<>();
        for (Member findMember : members) {
            boolean isFriend = friendFactory.isFriend(team.getHost().getId(), findMember.getId());
            MemberTeam isStatus = memberTeamRepository.findByTeamIdAndMemberId(team.getId(), findMember.getId()).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND_IN_TEAM));
            list.add(TeamMemberResponse.of(findMember, team, isFriend, isStatus));
        }
        return list;
    }

    private void checkTeamExists(Long teamId) {
        teamRepository.findByIdAndStatus(teamId, ACTIVE).orElseThrow(() -> new BaseException(TEAM_NOT_FOUND));
    }

    private void checkTeamInvitationAlreadyExists(Long teamId) {
        if (memberTeamRepository.existsByTeamIdAndMemberIdAndStatus(teamId, memberFacade.getCurrentMember().getId(), ACTIVE)) {
            throw new BaseException(ALREADY_ACCEPT_TEAM_INVITATION);
        }
    }

    private void inviteMemberToTeam(Long teamId) {
        MemberTeam memberTeam = memberTeamRepository.findByTeamIdAndMemberIdAndStatus(teamId, memberFacade.getCurrentMember().getId(), WAIT)
                .orElseThrow(() -> new BaseException(TEAM_INVITATION_NOT_FOUND));
        memberTeam.setStatus(ACTIVE);
        memberTeam.setParticipatedAt(new Date());
    }

    private void refuseTeamInvitation(Long teamId) {
        MemberTeam memberTeam = memberTeamRepository.findByTeamIdAndMemberIdAndStatus(teamId, memberFacade.getCurrentMember().getId(), WAIT)
                .orElseThrow(() -> new BaseException(TEAM_INVITATION_NOT_FOUND));
        memberTeam.setStatus(DELETE);
    }

    private Member checkManagerExistsAndMembership(Long teamId, TeamPlanRequest request) {
        Member manager = memberRepository.findByIdAndStatus(request.getManagerId(), ACTIVE).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));
        memberTeamRepository.findByTeamIdAndMemberId(teamId, manager.getId()).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND_IN_TEAM));
        return manager;
    }

    private void checkMakerExistsAndMemberShip(Long teamId) {
        Member maker = memberRepository.findByIdAndStatus(memberFacade.getCurrentMember().getId(), ACTIVE).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));;
        memberTeamRepository.findByTeamIdAndMemberId(teamId, maker.getId()).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND_IN_TEAM));
    }

    private Plan createPlan(Long teamId, Member manager, TeamPlanRequest request) {
        Team team = teamRepository.findByIdAndStatus(teamId, ACTIVE).orElseThrow(() -> new BaseException(TEAM_NOT_FOUND));
        Plan plan = request.toEntity();
        plan.setManager(manager);
        plan.setTeam(team);
        plan.setStatus(ACTIVE);
        plan.setAchievement(INCOMPLETE);
        return plan;
    }

    private Member checkCurrentMemberExistsInTeam(Long teamId) {
        Member currentMember = memberRepository.findByIdAndStatus(memberFacade.getCurrentMember().getId(), ACTIVE).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));
        memberTeamRepository.findByTeamIdAndMemberId(teamId, currentMember.getId()).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND_IN_TEAM));
        return currentMember;
    }

    private Member checkMandatorInTeam(Long teamId, TeamPlanPassRequest request) {
        Member mandator = memberRepository.findByIdAndStatus(request.getMandatorId(), ACTIVE).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));
        memberTeamRepository.findByTeamIdAndMemberId(teamId, mandator.getId()).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND_IN_TEAM));
        return mandator;
    }

    private Plan passPlan(TeamPlanPassRequest request, Member mandator) {
        Plan plan = planRepository.findById(request.getPlanId()).orElseThrow(() -> new BaseException(PLAN_NOT_FOUND));
        if (plan.getManager().getId() != memberFacade.getCurrentMember().getId()) {
            throw new BaseException(INVALID_MANAGER);
        }
        if (plan.getStatus() != ACTIVE) {
            throw new BaseException(INVALID_PLAN);
        }
        plan.setManager(mandator);
        return plan;
    }

    private Team checkMemberInTeam(Long teamId) {
        Team team = teamRepository.findByIdAndStatus(teamId, ACTIVE).orElseThrow(() -> new BaseException(TEAM_NOT_FOUND));
        Member member = memberRepository.findByIdAndStatus(memberFacade.getCurrentMember().getId(), ACTIVE).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));
        memberTeamRepository.findByTeamIdAndMemberId(teamId, member.getId()).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND_IN_TEAM));
        return team;
    }

    private void complete(Long teamId, Long planId) {
        Plan plan = planRepository.findByIdAndTeamIdAndStatus(planId, teamId, ACTIVE).orElseThrow(() -> new BaseException(PLAN_NOT_FOUND));
        if (plan.getManager().getId() != memberFacade.getCurrentMember().getId()) {
            throw new BaseException(INVALID_COMPLETE_PLAN);
        }
        plan.setAchievement(COMPLETE);
    }

    private void incomplete(Long teamId, Long planId) {
        Plan plan = planRepository.findByIdAndTeamIdAndStatus(planId, teamId, ACTIVE).orElseThrow(() -> new BaseException(PLAN_NOT_FOUND));
        if (plan.getAchievement() == INCOMPLETE) {
            throw new BaseException(ALREADY_INCOMPLETE_PLAN);
        }
        plan.setAchievement(INCOMPLETE);
    }

    private List<MemberResponse> getMembersByTeamId(Long teamId) {
        List<MemberResponse> memberList = memberTeamRepository.findALLByTeamIdAndStatus(teamId, ACTIVE).stream()
                .map(MemberTeam::getMember)
                .map(member -> MemberResponse.builder()
                        .memberId(member.getId())
                        .nickname(member.getNickname())
                        .profileImage(member.getProfileImage())
                        .build())
                .collect(Collectors.toList());
        return memberList;
    }

    private List<TeamPlanResponse> getPlansByDate(Long teamId, LocalDate date) {
        List<TeamPlanResponse> planList = planRepository
                .findAllByTeamIdAndDateAndStatus(teamId, date, ACTIVE)
                .stream()
                .map(plan -> TeamPlanResponse.builder()
                        .planId(plan.getId())
                        .managerId(plan.getManager().getId())
                        .title(plan.getTitle())
                        .date(plan.getDate())
                        .status(plan.getStatus())
                        .achievement(plan.getAchievement())
                        .build()
                )
                .collect(Collectors.toList());
        return planList;
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
}

