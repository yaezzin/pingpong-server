package com.app.pingpong.domain.team.service;

import com.app.pingpong.domain.friend.repository.FriendQueryRepository;
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
import com.app.pingpong.global.common.exception.BaseException;
import com.app.pingpong.global.common.exception.StatusCode;
import com.app.pingpong.global.common.status.Status;
import com.app.pingpong.global.common.util.MemberFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.app.pingpong.global.common.exception.StatusCode.*;
import static com.app.pingpong.global.common.status.Status.*;

@RequiredArgsConstructor
@Service
public class TeamService {

    private final MemberRepository memberRepository;
    private final FriendQueryRepository friendQueryRepository;
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
        setTeamToMembers(team, request);

        return TeamResponse.of(memberTeamRepository.findAllByTeamId(team.getId()));
    }

    @Transactional
    public TeamResponse update(Long id, TeamRequest request) {
        Team team = teamRepository.findByIdAndStatus(id, ACTIVE).orElseThrow(() -> new BaseException(TEAM_NOT_FOUND));

        checkHost(team.getHost());
        team.setName(request.getName());
        setTeamToMembers(team, request);

        // 나랑 친구인지 여부 확인하는 예외처리 필요?

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
    public TeamHostResponse updateHost(Long teamId, Long delegatorId, Long loginMemberId) {
        Team team = checkHostForDelegate(teamId, loginMemberId, delegatorId);
        return TeamHostResponse.of(team, getTeamMemberStatus(team));
    }

    @Transactional
    public TeamHostResponse emit(Long teamId, Long emitterId) {
        Team team = checkHostForEmit(teamId, emitterId);
        emitMember(teamId, emitterId);
        return TeamHostResponse.of(team, getTeamMemberStatus(team));
    }

    /* host 관점 */
    @Transactional
    public List<TeamMemberResponse> getTeamMembers(Long teamId) {
        Team team = teamRepository.findByIdAndStatus(teamId, ACTIVE).orElseThrow(() -> new BaseException(TEAM_NOT_FOUND));
        List<MemberTeam> memberTeam = memberTeamRepository.findAllByTeamId(teamId);
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
    public StatusCode resign(Long teamId, Long loginMemberId) {
        Team team = checkTeamExists(teamId);
        MemberTeam memberTeam = checkMemberInTeam(teamId, loginMemberId);
        checkHostForResign(team, loginMemberId);
        checkMemberStatusInTeam(memberTeam);
        memberTeam.setStatus(DELETE);
        return SUCCESS_RESIGN_TEAM;
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
        Member member = memberRepository.findByIdAndStatus(memberFacade.getCurrentMember().getId(), ACTIVE).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));
        memberTeamRepository.findByTeamIdAndMemberId(teamId, member.getId()).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND_IN_TEAM));
        Plan plan = planRepository.findById(planId).orElseThrow(() -> new BaseException(PLAN_NOT_FOUND));
        plan.setStatus(DELETE);
        return TeamPlanResponse.of(plan);
    }

    @Transactional
    public TeamPlanResponse passPlan(Long teamId, Long loginMemberId, TeamPlanPassRequest request) {
        checkMemberInTeam(teamId, loginMemberId);
        Member mandator = checkMandatorInTeam(teamId, request);
        Plan plan = passPlan(request, mandator);
        return TeamPlanResponse.of(plan);
    }

    @Transactional
    public StatusCode completePlan(Long teamId, Long planId, Long loginMemberId) {
        checkMemberInTeam(teamId, loginMemberId);
        complete(teamId, planId);
        return SUCCESS_COMPLETE_PLAN;
    }

    @Transactional
    public StatusCode incompletePlan(Long teamId, Long planId, Long loginMemberId) {
        checkMemberInTeam(teamId, loginMemberId);
        incomplete(teamId, planId);
        return SUCCESS_INCOMPLETE_PLAN;
    }

    @Transactional(readOnly = true)
    public TeamPlanDetailResponse getTeamCalendarByDate(Long teamId, LocalDate date, Long loginMemberId) {
        Team team = checkTeamExists(teamId);
        checkMemberInTeam(teamId, loginMemberId);
        List<MemberResponse> memberList = getMembersByTeamId(teamId);
        List<TeamPlanResponse> planList = getPlansByDate(teamId, date);
        return TeamPlanDetailResponse.of(team, memberList, planList);
    }

    @Transactional
    public List<TeamAchieveResponse> getTeamAchievementRate(Long teamId, TeamAchieveRequest request) {
        List<LocalDate> dateList = getPlanDateList(teamId, request);
        List<TeamAchieveResponse> achieveRate = getTeamPlanAchieve(teamId, dateList);
        return achieveRate;
    }

    private List<LocalDate> getPlanDateList(Long teamId, TeamAchieveRequest request) {
        List<LocalDate> dateList = planRepository.findAllByTeamIdAndStatusAndDateBetween(teamId, ACTIVE, request.getStartDate(), request.getEndDate())
                .stream()
                .map(Plan::getDate)
                .distinct()
                .collect(Collectors.toList());
        return dateList;
    }

    private List<TeamAchieveResponse> getTeamPlanAchieve(Long teamId, List<LocalDate> dateList) {
        return dateList.stream().map(date -> {
            List<Plan> plans = planRepository.findAllByTeamIdAndDateAndStatus(teamId, date, ACTIVE);
            long complete = plans.stream().filter(plan -> plan.getAchievement() == COMPLETE).count();
            long incomplete = plans.size() - complete;
            double achievement = (complete + incomplete) == 0 ? 0 : ((double) complete / (double) (complete + incomplete) * 100.0);
            return new TeamAchieveResponse(date, achievement);
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TeamPlanResponse> getTrash(Long teamId, Long loginMemberId) {
        List<Plan> plansInTrash = getTrashByAuth(teamId, loginMemberId);
        return TeamPlanResponse.of(plansInTrash);
    }

    @Transactional
    public StatusCode deleteAllTrash(Long teamId, Long loginMemberId) {
        checkAuthForDelete(teamId, loginMemberId);
        deleteAllTrashPermanent(teamId);
        return SUCCESS_DELETE_ALL_TRASH;
    }

    @Transactional
    public StatusCode deleteTrash(Long teamId, Long planId, Long loginMemberId) {
        checkAuthForDelete(teamId, loginMemberId);
        deleteTrashPermanent(planId);
        return SUCCESS_DELETE_TRASH;
    }

    @Transactional
    public StatusCode recoverTrash(Long teamId, Long planId, Long loginMemberId) {
        checkTeamExists(teamId);
        checkMemberInTeam(teamId, loginMemberId);
        recover(planId);
        return SUCCESS_RECOVER_TRASH;
    }

    private void checkTeam(Member loginMember, TeamRequest request) {
        if (teamRepository.findAllByHostId(loginMember.getId()).size() > 6) {
            throw new BaseException(EXCEED_HOST_TEAM_SIZE);
        }
        if (request.getMemberId().size() > 10 || request.getMemberId().size() < 1) {
            throw new BaseException(INVALID_TEAM_MEMBER_SIZE);
        }
        if (request.getMemberId().contains(loginMember.getId())) {
            throw new BaseException(INVALID_TEAM_HOST_MEMBER);
        }
        for (Long id : request.getMemberId()) {
            memberRepository.findByIdAndStatus(id, ACTIVE).orElseThrow(() -> new BaseException(INVALID_INVITER));
            friendQueryRepository.checkFriendship(loginMember.getId(), id);
        }
    }

    private void setTeam(Team team, Member host) {
        team.setHost(host);
        team.setStatus(Status.ACTIVE);
    }

    private void setTeamToHost(Team team, Member loginMember) {
        MemberTeam memberTeam = new MemberTeam();
        memberTeam.setTeam(team);
        memberTeam.setMember(loginMember);
        memberTeam.setStatus(Status.ACTIVE);
        memberTeamRepository.save(memberTeam);
    }

    private void setTeamToMembers(Team newTeam, TeamRequest request) {
        request.getMemberId().stream()
                .map(memberRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(member -> {
                    if (isMemberAlreadyInTeamWithStatus(newTeam, member, WAIT) || isMemberAlreadyInTeamWithStatus(newTeam, member, ACTIVE)) {
                        throw new BaseException(ALREADY_INVITE_TEAM);
                    }
                    MemberTeam memberTeam = new MemberTeam();
                    memberTeam.setTeam(newTeam);
                    memberTeam.setMember(member);
                    memberTeam.setStatus(WAIT);
                    memberTeamRepository.save(memberTeam);
                });
    }

    private boolean isMemberAlreadyInTeamWithStatus(Team team, Member member, Status status) {
        return memberTeamRepository.existsByTeamIdAndMemberIdAndStatus(team.getId(), member.getId(), status);
    }

    private void checkHost(Member host) {
        if (!memberFacade.getCurrentMember().equals(host)) {
            throw new BaseException(INVALID_HOST);
        }
    }

    private void deleteTeamAndMembers(Team team) {
        team.setStatus(DELETE);
        List<MemberTeam> memberTeams = memberTeamRepository.findAllByTeamId(team.getId());
        memberTeams.forEach(memberTeam -> memberTeam.setStatus(DELETE));
    }

    private Team checkHostForDelegate(Long teamId, Long loginMemberId, Long delegatorId) {
        Team team = teamRepository.findByIdAndStatus(teamId, ACTIVE).orElseThrow(() -> new BaseException(TEAM_NOT_FOUND));
        Member host = memberRepository.findByIdAndStatus(loginMemberId, ACTIVE).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));
        Member delegator = memberRepository.findByIdAndStatus(delegatorId, ACTIVE).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));
        memberTeamRepository.findByTeamIdAndMemberIdAndStatus(teamId, delegatorId, ACTIVE).orElseThrow(() -> new BaseException(DELEGATOR_NOT_FOUND_IN_TEAM));

        if (!team.getHost().equals(host)) {
            throw new BaseException(INVALID_HOST);
        } else if (team.getHost().equals(delegator)) {
            throw new BaseException(ALREADY_TEAM_HOST);
        }

        team.setHost(delegator);
        return team;
    }

    private List<TeamCompactResponse> getTeamMemberStatus(Team team) {
        List<TeamCompactResponse> list = new ArrayList<>();
        List<MemberTeam> all = memberTeamRepository.findAllByTeamId(team.getId());
        for (MemberTeam mt : all) {
            list.add(TeamCompactResponse.builder().memberId(mt.getMember().getId()).status(mt.getStatus()).build());
        }
        return list;
    }

    private Team checkHostForEmit(Long teamId, Long emitterId) {
        Team team = teamRepository.findByIdAndStatus(teamId, ACTIVE).orElseThrow(() -> new BaseException(TEAM_NOT_FOUND));
        Member host = memberRepository.findByIdAndStatus(memberFacade.getCurrentMember().getId(), ACTIVE).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));
        Member emitter = memberRepository.findByIdAndStatus(emitterId, ACTIVE).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));

        if (!team.getHost().equals(host)) {
            throw new BaseException(INVALID_HOST);
        }
        if (host.equals(emitter)) {
            throw new BaseException(INVALID_EMITTER);
        }
        return team;
    }

    private void emitMember(Long teamId, Long emitterId) {
        MemberTeam memberTeam = memberTeamRepository.findByTeamIdAndMemberId(teamId, emitterId).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND_IN_TEAM));

        Status status = memberTeam.getStatus();
        if (status.equals(ACTIVE)) {
            memberTeam.setStatus(DELETE);
        } else if (status.equals(WAIT)) {
            throw new BaseException(INVALID_EMITTER);
        } else if (status.equals(DELETE)) {
            throw new BaseException(MEMBER_ALREADY_EMIT);
        }
    }

    private List<Member> getMembersFromMemberTeams(List<MemberTeam> memberTeams) {
        return memberTeams.stream()
                .map(MemberTeam::getMember)
                .filter(member -> member.getStatus().equals(ACTIVE))
                .collect(Collectors.toList());
    }

    private List<TeamMemberResponse> buildTeamMemberResponseList(List<Member> members, Team team) {
        List<TeamMemberResponse> list = new ArrayList<>();
        Long loginMemberId = memberFacade.getCurrentMember().getId();

        for (Member findMember : members) {
            Status friendStatus = friendQueryRepository.findFriendStatus(loginMemberId, findMember.getId());
            MemberTeam status = memberTeamRepository.findByTeamIdAndMemberId(team.getId(), findMember.getId()).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND_IN_TEAM));
            list.add(TeamMemberResponse.of(findMember, team, friendStatus, status));
        }
        return list;
    }

    private Team checkTeamExists(Long teamId) {
        return teamRepository.findByIdAndStatus(teamId, ACTIVE).orElseThrow(() -> new BaseException(TEAM_NOT_FOUND));
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

    private MemberTeam checkMemberInTeam(Long teamId, Long loginMemberId) {
        return memberTeamRepository.findByTeamIdAndMemberId(teamId, loginMemberId).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND_IN_TEAM));
    }

    private void checkHostForResign(Team team, Long loginMemberId) {
        Member loginMember = memberRepository.findByIdAndStatus(loginMemberId, ACTIVE).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));
        if (team.getHost().equals(loginMember)) {
            throw new BaseException(INVALID_RESIGN);
        }
    }

    private void checkMemberStatusInTeam(MemberTeam memberTeam) {
        if (memberTeam.getStatus().equals(WAIT) || memberTeam.getStatus().equals(DELETE)) {
            throw new BaseException(INVALID_RESIGN_STATUS);
        }
    }

    private Member checkManagerExistsAndMembership(Long teamId, TeamPlanRequest request) {
        Member manager = memberRepository.findByIdAndStatus(request.getManagerId(), ACTIVE).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));
        memberTeamRepository.findByTeamIdAndMemberIdAndStatus(teamId, manager.getId(), ACTIVE).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND_IN_TEAM));
        return manager;
    }

    private void checkMakerExistsAndMemberShip(Long teamId) {
        Member maker = memberRepository.findByIdAndStatus(memberFacade.getCurrentMember().getId(), ACTIVE).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));
        memberTeamRepository.findByTeamIdAndMemberIdAndStatus(teamId, maker.getId(), ACTIVE).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND_IN_TEAM));
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

    private Member checkMandatorInTeam(Long teamId, TeamPlanPassRequest request) {
        Member mandator = memberRepository.findByIdAndStatus(request.getMandatorId(), ACTIVE).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));
        memberTeamRepository.findByTeamIdAndMemberId(teamId, mandator.getId()).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND_IN_TEAM));
        return mandator;
    }

    private Plan passPlan(TeamPlanPassRequest request, Member mandator) {
        Plan plan = planRepository.findById(request.getPlanId()).orElseThrow(() -> new BaseException(PLAN_NOT_FOUND));

        if (!plan.getManager().equals(memberFacade.getCurrentMember())) {
            throw new BaseException(INVALID_MANAGER);
        }
        if (plan.getStatus() != ACTIVE) {
            throw new BaseException(INVALID_PLAN);
        }
        plan.setManager(mandator);
        return plan;
    }

    private void complete(Long teamId, Long planId) {
        Plan plan = planRepository.findByIdAndTeamIdAndStatus(planId, teamId, ACTIVE).orElseThrow(() -> new BaseException(PLAN_NOT_FOUND));
        if (!plan.getManager().equals(memberFacade.getCurrentMember())) {
            throw new BaseException(INVALID_COMPLETE_PLAN);
        }
        if (plan.getAchievement().equals(COMPLETE)) {
            throw new BaseException(ALREADY_COMPLETE_PLAN);
        }
        plan.setAchievement(COMPLETE);
    }

    private void incomplete(Long teamId, Long planId) {
        Plan plan = planRepository.findByIdAndTeamIdAndStatus(planId, teamId, ACTIVE).orElseThrow(() -> new BaseException(PLAN_NOT_FOUND));
        if (!plan.getManager().equals(memberFacade.getCurrentMember())) {
            throw new BaseException(INVALID_INCOMPLETE_PLAN);
        }
        if (plan.getAchievement().equals(INCOMPLETE)) {
            throw new BaseException(ALREADY_INCOMPLETE_PLAN);
        }
        plan.setAchievement(INCOMPLETE);
    }

    private List<MemberResponse> getMembersByTeamId(Long teamId) {
        List<MemberResponse> memberList = memberTeamRepository.findAllByTeamIdAndStatus(teamId, ACTIVE).stream()
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
                        .title(plan.getTitle())
                        .date(plan.getDate())
                        .status(plan.getStatus())
                        .achievement(plan.getAchievement())
                        .manager(MemberResponse.of(plan.getManager()))
                        .build()
                )
                .collect(Collectors.toList());
        return planList;
    }

    private List<Plan> getTrashByAuth(Long teamId, Long loginMemberId) {
        Team team = teamRepository.findByIdAndStatus(teamId, ACTIVE).orElseThrow(() -> new BaseException(TEAM_NOT_FOUND));
        Member loginMember = memberRepository.findByIdAndStatus(loginMemberId, ACTIVE).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));
        memberTeamRepository.findByTeamIdAndMemberId(team.getId(), loginMemberId).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND_IN_TEAM));

        List<Plan> plansInTrash;
        if (loginMember.equals(team.getHost())) { // 방장이라면 팀에 대한 모든 삭제된 일정 가져옴
            plansInTrash = planRepository.findAllByTeamIdAndStatusOrderByWastedTimeDesc(team.getId(), DELETE)
                    .orElseThrow(() -> new BaseException(ALREADY_DELETE_TRASH));
        } else { // 방장이 아니면 내가 담당자인것만 가져옴옴
            plansInTrash = planRepository.findAllByManagerIdAndTeamIdAndStatusOrderByWastedTimeDesc(loginMemberId, team.getId(), DELETE);
        }
        return plansInTrash;
    }

    private void checkAuthForDelete(Long teamId, Long loginMemberId) {
        Member loginMember = memberRepository.findByIdAndStatus(loginMemberId, ACTIVE).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));
        Team team = teamRepository.findByIdAndStatus(teamId, ACTIVE).orElseThrow(() -> new BaseException(TEAM_NOT_FOUND));
        memberTeamRepository.findByTeamIdAndMemberId(teamId, loginMemberId).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND_IN_TEAM));
        if (!loginMember.equals(team.getHost())) {
            throw new BaseException(INVALID_HOST);
        }
    }

    private void deleteAllTrashPermanent(Long teamId) {
        List<Plan> plansInTrash = planRepository.findAllByTeamIdAndStatusOrderByWastedTimeDesc(teamId, DELETE)
                .orElseThrow(() -> new BaseException(ALREADY_DELETE_TRASH));
        if (plansInTrash.isEmpty()) {
            throw new BaseException(PLANS_IN_TRASH_NOT_FOUND);
        }
        for (Plan p : plansInTrash) {
            p.setStatus(PERMANENT);
        }
    }

    private void deleteTrashPermanent(Long planId) {
        Plan plan = planRepository.findByIdAndStatus(planId, DELETE).orElseThrow(() -> new BaseException(PLAN_NOT_FOUND));
        plan.setStatus(PERMANENT);
    }

    private void recover(Long planId) {
        Plan plan = planRepository.findByIdAndStatus(planId, DELETE).orElseThrow(() -> new BaseException(PLAN_NOT_FOUND));
        plan.setStatus(ACTIVE);
    }
}

