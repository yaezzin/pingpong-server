package com.app.pingpong.domain.team.service;

import com.app.pingpong.domain.friend.repository.FriendFactory;
import com.app.pingpong.domain.member.entity.Member;
import com.app.pingpong.domain.member.entity.MemberTeam;
import com.app.pingpong.domain.member.repository.MemberRepository;
import com.app.pingpong.domain.member.repository.MemberTeamRepository;
import com.app.pingpong.domain.team.dto.request.TeamPlanPassRequest;
import com.app.pingpong.domain.team.dto.request.TeamPlanRequest;
import com.app.pingpong.domain.team.dto.request.TeamRequest;
import com.app.pingpong.domain.team.dto.response.TeamHostResponse;
import com.app.pingpong.domain.team.dto.response.TeamMemberResponse;
import com.app.pingpong.domain.team.dto.response.TeamPlanResponse;
import com.app.pingpong.domain.team.dto.response.TeamResponse;
import com.app.pingpong.domain.team.entity.Plan;
import com.app.pingpong.domain.team.entity.Team;
import com.app.pingpong.domain.team.repository.PlanRepository;
import com.app.pingpong.domain.team.repository.TeamRepository;
import com.app.pingpong.global.common.exception.BaseException;
import com.app.pingpong.global.common.exception.StatusCode;
import com.app.pingpong.global.common.util.MemberFacade;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.app.pingpong.factory.MemberFactory.createMember;
import static com.app.pingpong.factory.MemberTeamFactory.*;
import static com.app.pingpong.factory.PlanFactory.*;
import static com.app.pingpong.factory.TeamFactory.createTeam;
import static com.app.pingpong.factory.TeamFactory.createTeamList;
import static com.app.pingpong.global.common.exception.StatusCode.*;
import static com.app.pingpong.global.common.status.Status.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class TeamServiceMockTest {

    @InjectMocks
    TeamService teamService;

    @Mock
    MemberRepository memberRepository;

    @Mock
    TeamRepository teamRepository;

    @Mock
    MemberTeamRepository memberTeamRepository;

    @Mock
    PlanRepository planRepository;

    @Mock
    FriendFactory friendFactory;

    @Mock
    MemberFacade memberFacade;

    @Test
    public void create() {
        // given
        Member host = createMember();
        Member member = createMember();

        Team team = createTeam(host);
        MemberTeam memberTeamForHost = createMemberTeam(host, team);
        MemberTeam memberTeamForMember = createWaitMemberTeam(member, team);

        TeamRequest request = new TeamRequest("team-name", List.of(1L, 2L));

        given(memberFacade.getCurrentMember()).willReturn(member);
        given(memberRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(member));
        given(teamRepository.save(any())).willReturn(team);
        given(memberTeamRepository.findAllByTeamId(any())).willReturn(List.of(memberTeamForHost, memberTeamForMember));

        // when
        TeamResponse response = teamService.create(request);

        // then
        verify(teamRepository, times(1)).save(any());
        verify(memberTeamRepository, times(1)).save(any());
        assertThat(memberTeamForHost.getStatus()).isEqualTo(ACTIVE);
        assertThat(memberTeamForMember.getStatus()).isEqualTo(WAIT);
    }

    @Test
    public void createExceptionByExceedHostTeamSize() {
        // given
        Member member = createMember();
        TeamRequest request = new TeamRequest("team-name", List.of(1L));

        int exceedHostSize = 7;
        List<Team> exceedTeamList = createTeamList(member, exceedHostSize);

        given(memberFacade.getCurrentMember()).willReturn(member);
        given(teamRepository.findByHostId(any())).willReturn(exceedTeamList);

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> teamService.create(request));
        assertThat(exception.getStatus()).isEqualTo(EXCEED_HOST_TEAM_SIZE);
    }

    @Test
    public void createExceptionByExceedTeamMemberSize() {
        // given
        Member member = createMember();
        TeamRequest request = new TeamRequest("team-name", List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L));

        given(memberFacade.getCurrentMember()).willReturn(member);

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> teamService.create(request));
        assertThat(exception.getStatus()).isEqualTo(INVALID_TEAM_MEMBER_SIZE);
    }

    @Test
    public void createExceptionByDeficientTeamMemberSize() {
        // given
        Member member = createMember();
        TeamRequest request = new TeamRequest("team-name", Collections.emptyList());

        given(memberFacade.getCurrentMember()).willReturn(member);

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> teamService.create(request));
        assertThat(exception.getStatus()).isEqualTo(INVALID_TEAM_MEMBER_SIZE);
    }

    @Test
    public void createExceptionByInvalidInviter() {
        Member member = createMember();
        TeamRequest request = new TeamRequest("team-name", List.of(1L, 2L));

        given(memberFacade.getCurrentMember()).willReturn(member);
        given(memberRepository.findByIdAndStatus(any(), any())).willReturn(Optional.empty());

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> teamService.create(request));
        assertThat(exception.getStatus()).isEqualTo(INVALID_INVITER);
    }

    @Test
    public void delete() {
        // given
        Member host = createMember();
        Team team = createTeam(host);
        MemberTeam memberTeam = createMemberTeam(host, team);

        given(teamRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(team));
        given(memberFacade.getCurrentMember()).willReturn(host);
        given(memberTeamRepository.findAllByTeamId(any())).willReturn(List.of(memberTeam));

        // when
        StatusCode code = teamService.delete(1L);

        // then
        assertThat(code).isEqualTo(SUCCESS_DELETE_TEAM);
        assertThat(team.getStatus()).isEqualTo(DELETE);
        assertThat(memberTeam.getStatus()).isEqualTo(DELETE);
    }

    @Test
    public void deleteExceptionByTeamNotFound() {
        // given
        given(teamRepository.findByIdAndStatus(any(), any())).willReturn(Optional.empty());

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> teamService.delete(1L));
        assertThat(exception.getStatus()).isEqualTo(TEAM_NOT_FOUND);
    }

    @Test
    public void deleteExceptionByHostNotFound() {
        // given
        Member host = createMember();
        Member member = createMember();
        Team team = createTeam(host);

        given(teamRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(team));
        given(memberFacade.getCurrentMember()).willReturn(member);

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> teamService.delete(1L));
        assertThat(exception.getStatus()).isEqualTo(INVALID_HOST);
    }

    @Test
    public void updateHost() {
        // given
        Member host = createMember();
        Member delegator = createMember();
        Team team = createTeam(host);
        MemberTeam memberTeam = createMemberTeam(host, team);

        given(teamRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(team));
        given(memberRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(host)).willReturn(Optional.of(delegator));
        given(memberTeamRepository.findAllByTeamId(any())).willReturn(List.of(memberTeam));

        // when
        teamService.updateHost(1L, 2L, 3L);

        // then
        verify(memberRepository, times(2)).findByIdAndStatus(any(), any());
    }

    @Test
    public void updateHostExceptionByTeamNotFound() {
        // given
        given(teamRepository.findByIdAndStatus(any(), any())).willReturn(Optional.empty());

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> teamService.updateHost(1L, 2L, 3L));
        assertThat(exception.getStatus()).isEqualTo(TEAM_NOT_FOUND);
    }

    @Test
    public void updateHostExceptionByHostNotFound() {
        // given
        Member host = createMember();
        Team team = createTeam(host);

        given(teamRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(team));
        given(memberRepository.findByIdAndStatus(any(), any())).willReturn(Optional.empty());

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> teamService.updateHost(1L, 2L, 3L));
        assertThat(exception.getStatus()).isEqualTo(MEMBER_NOT_FOUND);
    }

    @Test
    public void updateHostExceptionByDelegatorNotFound() {
        // given
        Member host = createMember();
        Member delegator = createMember();
        Team team = createTeam(host);

        given(teamRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(team));
        given(memberRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(host)).willReturn(Optional.empty());

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> teamService.updateHost(1L, 2L, 3L));
        assertThat(exception.getStatus()).isEqualTo(MEMBER_NOT_FOUND);
    }

    @Test
    public void updateHostExceptionByInvalidHost() {
        // given
        Member host = createMember();
        Member delegator = createMember();
        Team team = createTeam(host);

        given(teamRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(team));
        given(memberRepository.findByIdAndStatus(anyLong(), any())).willReturn(Optional.of(delegator));

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> teamService.updateHost(1L, 2L, 3L));
        assertThat(exception.getStatus()).isEqualTo(INVALID_HOST);
    }

    @Test
    public void updateHostExceptionByAlreadyHost() {
        // given
        Member host = createMember();
        Member delegator = createMember();
        Team team = createTeam(host);

        given(teamRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(team));
        given(memberRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(host));

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> teamService.updateHost(1L, 2L, 3L));
        assertThat(exception.getStatus()).isEqualTo(ALREADY_TEAM_HOST);
    }

    @Test
    public void emit() {
        // given
        Member host = createMember();
        Member emitter = createMember();
        Team team = createTeam(host);
        MemberTeam memberTeamForHost = createMemberTeam(host, team);
        MemberTeam memberTeamForEmitter = createMemberTeam(emitter, team);

        given(teamRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(team));
        given(memberFacade.getCurrentMember()).willReturn(host);
        given(memberRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(host)).willReturn(Optional.of(emitter));
        given(memberTeamRepository.findByTeamIdAndMemberIdAndStatus(any(), any(), any())).willReturn(Optional.of(memberTeamForEmitter));
        given(memberTeamRepository.findAllByTeamId(any())).willReturn(List.of(memberTeamForHost, memberTeamForEmitter));

        // when
        TeamHostResponse response = teamService.emit(1L, 2L);

        // then
        assertThat(response.getResponses().get(1).getStatus()).isEqualTo(DELETE);
    }

    @Test
    public void emitExceptionByTeamNotFound() {
        // given
        Member host = createMember();
        Team team = createTeam(host);

        given(teamRepository.findByIdAndStatus(any(), any())).willReturn(Optional.empty());

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> teamService.emit(1L, 2L));
        assertThat(exception.getStatus()).isEqualTo(TEAM_NOT_FOUND);
    }

    @Test
    public void emitExceptionByHostNotFound() {
        // given
        Member host = createMember();
        Team team = createTeam(host);

        given(teamRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(team));
        given(memberFacade.getCurrentMember()).willReturn(host);
        given(memberRepository.findByIdAndStatus(any(), any())).willReturn(Optional.empty());

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> teamService.emit(1L, 2L));
        assertThat(exception.getStatus()).isEqualTo(MEMBER_NOT_FOUND);
    }

    @Test
    public void emitExceptionByEmitterNotFound() {
        // given
        Member host = createMember();
        Team team = createTeam(host);

        given(teamRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(team));
        given(memberFacade.getCurrentMember()).willReturn(host);
        given(memberRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(host)).willReturn(Optional.empty());

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> teamService.emit(1L, 2L));
        assertThat(exception.getStatus()).isEqualTo(MEMBER_NOT_FOUND);
    }

    @Test
    public void emitExceptionByMemberAlreadyEmit() {
        // given
        Member host = createMember();
        Member emitter = createMember();
        Team team = createTeam(host);

        given(teamRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(team));
        given(memberFacade.getCurrentMember()).willReturn(host);
        given(memberRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(host)).willReturn(Optional.of(emitter));
        given(memberTeamRepository.findByTeamIdAndMemberIdAndStatus(any(), any(), eq(ACTIVE))).willReturn(Optional.empty());

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> teamService.emit(1L, 2L));
        assertThat(exception.getStatus()).isEqualTo(MEMBER_ALREADY_EMIT);
    }

    @Test
    public void emitExceptionByInvalidHost() {
        // given
        Member host = createMember();
        Member emitter = createMember();
        Team team = createTeam(host);

        given(teamRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(team));
        given(memberFacade.getCurrentMember()).willReturn(host);
        given(memberRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(emitter)).willReturn(Optional.of(emitter));

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> teamService.emit(1L, 2L));
        assertThat(exception.getStatus()).isEqualTo(INVALID_HOST);
    }

    @Test
    public void emitExceptionByInvalidEmitter() {
        // given
        Member host = createMember();
        Member emitter = createMember();
        Team team = createTeam(host);

        given(teamRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(team));
        given(memberFacade.getCurrentMember()).willReturn(host);
        given(memberRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(host)).willReturn(Optional.of(host));

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> teamService.emit(1L, 2L));
        assertThat(exception.getStatus()).isEqualTo(INVALID_EMITTER);
    }

    @Test
    public void getTeamMembers() {
        // given
        Member host = createMember();
        Member member = createMember();
        Team team = createTeam(host);
        MemberTeam memberTeamForHost = createMemberTeam(host, team);
        MemberTeam memberTeamForMember = createMemberTeam(member, team);

        given(teamRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(team));
        given(memberTeamRepository.findAllByTeamId(any())).willReturn(List.of(memberTeamForHost, memberTeamForMember));
        given(friendFactory.isFriend(any(), any())).willReturn(false);
        given(memberTeamRepository.findByTeamIdAndMemberId(any(), any())).willReturn(Optional.of(memberTeamForHost));

        // when
        List<TeamMemberResponse> response = teamService.getTeamMembers(1L);

        // then
        assertThat(response.size()).isEqualTo(2);
    }

    @Test
    public void getTeamMembersExceptionByTeamNotFound() {
        Member host = createMember();
        Team team = createTeam(host);

        given(teamRepository.findByIdAndStatus(any(), any())).willReturn(Optional.empty());

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> teamService.getTeamMembers(1L));
        assertThat(exception.getStatus()).isEqualTo(TEAM_NOT_FOUND);
    }

    @Test
    public void accept() {
        // given
        Member member = createMember();
        Team team = createTeam(member);
        MemberTeam memberTeam = createWaitMemberTeam(member, team);

        given(memberFacade.getCurrentMember()).willReturn(member);
        given(teamRepository.findByIdAndStatus(anyLong(), any())).willReturn(Optional.of(team));
        given(memberTeamRepository.existsByTeamIdAndMemberIdAndStatus(any(), any(), eq(ACTIVE))).willReturn(false);
        given(memberTeamRepository.findByTeamIdAndMemberIdAndStatus(any(), any(), eq(WAIT))).willReturn(Optional.of(memberTeam));

        // when
        StatusCode code = teamService.accept(1L);

        // then
        assertThat(code).isEqualTo(SUCCESS_ACCEPT_TEAM_INVITATION);
    }

    @Test
    public void acceptExceptionByTeamNotFound() {
        // given
        Member member = createMember();
        Team team = createTeam(member);
        MemberTeam memberTeam = createWaitMemberTeam(member, team);

        given(teamRepository.findByIdAndStatus(anyLong(), any())).willReturn(Optional.empty());

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> teamService.accept(1L));
        assertThat(exception.getStatus()).isEqualTo(TEAM_NOT_FOUND);
    }

    @Test
    public void acceptExceptionByAlreadyAccept() {
        // given
        Member member = createMember();
        Team team = createTeam(member);
        MemberTeam memberTeam = createWaitMemberTeam(member, team);

        given(memberFacade.getCurrentMember()).willReturn(member);
        given(teamRepository.findByIdAndStatus(anyLong(), any())).willReturn(Optional.of(team));
        given(memberTeamRepository.existsByTeamIdAndMemberIdAndStatus(any(), any(), eq(ACTIVE))).willReturn(true);

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> teamService.accept(1L));
        assertThat(exception.getStatus()).isEqualTo(ALREADY_ACCEPT_TEAM_INVITATION);
    }

    @Test
    public void acceptExceptionByTeamInvitationNotFound() {
        // given
        Member member1 = createMember();
        Member member2 = createMember();
        Team team = createTeam(member1);

        given(memberFacade.getCurrentMember()).willReturn(member1);
        given(teamRepository.findByIdAndStatus(anyLong(), any())).willReturn(Optional.of(team));
        given(memberTeamRepository.existsByTeamIdAndMemberIdAndStatus(any(), any(), eq(ACTIVE))).willReturn(false);
        given(memberTeamRepository.findByTeamIdAndMemberIdAndStatus(any(), any(), eq(WAIT))).willReturn(Optional.empty());

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> teamService.accept(1L));
        assertThat(exception.getStatus()).isEqualTo(TEAM_INVITATION_NOT_FOUND);
    }

    @Test
    public void refuse() {
        // given
        Member member = createMember();
        Team team = createTeam(member);
        MemberTeam memberTeam = createWaitMemberTeam(member, team);

        given(memberFacade.getCurrentMember()).willReturn(member);
        given(teamRepository.findByIdAndStatus(anyLong(), any())).willReturn(Optional.of(team));
        given(memberTeamRepository.findByTeamIdAndMemberIdAndStatus(any(), any(), eq(WAIT))).willReturn(Optional.of(memberTeam));

        // when
        StatusCode code = teamService.refuse(1L);

        // then
        assertThat(code).isEqualTo(SUCCESS_REFUSE_TEAM_INVITATION);
    }

    @Test
    public void refuseExceptionByTeamInvitationNotFound() {
        // given
        Member member = createMember();
        Team team = createTeam(member);
        MemberTeam memberTeam = createWaitMemberTeam(member, team);

        given(memberFacade.getCurrentMember()).willReturn(member);
        given(teamRepository.findByIdAndStatus(anyLong(), any())).willReturn(Optional.of(team));
        given(memberTeamRepository.findByTeamIdAndMemberIdAndStatus(any(), any(), eq(WAIT))).willReturn(Optional.empty());

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> teamService.refuse(1L));
        assertThat(exception.getStatus()).isEqualTo(TEAM_INVITATION_NOT_FOUND);
    }

    @Test
    public void resign() {
        // given
        Member host = createMember(); // 호스트
        Member member = createMember(); // 현재 로그인 유저?

        Team team = createTeam(host);
        MemberTeam memberTeamForMember = createMemberTeam(member, team);

        given(teamRepository.findByIdAndStatus(anyLong(), any())).willReturn(Optional.of(team));
        given(memberTeamRepository.findByTeamIdAndMemberId(anyLong(), anyLong())).willReturn(Optional.of(memberTeamForMember));
        given(memberRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(member));

        // when
        StatusCode code = teamService.resign(1L, 1L);

        // then
        assertThat(code).isEqualTo(SUCCESS_RESIGN_TEAM);
    }

    @Test
    public void resignExceptionByNotAcceptTeamInvitation() {
        // given
        Member host = createMember();
        Member member = createMember();
        Team team = createTeam(host);
        MemberTeam memberTeam = createWaitMemberTeam(member, team);

        given(teamRepository.findByIdAndStatus(anyLong(), any())).willReturn(Optional.of(team));
        given(memberTeamRepository.findByTeamIdAndMemberId(anyLong(), anyLong())).willReturn(Optional.of(memberTeam));
        given(memberRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(member));

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> teamService.resign(1L, 1L));
        assertThat(exception.getStatus()).isEqualTo(INVALID_RESIGN_STATUS);
    }

    @Test
    public void resignExceptionByAlreadyResign() {
        // given
        Member host = createMember();
        Member member = createMember();
        Team team = createTeam(host);
        MemberTeam memberTeam = createDeleteMemberTeam(member, team);

        given(teamRepository.findByIdAndStatus(anyLong(), any())).willReturn(Optional.of(team));
        given(memberTeamRepository.findByTeamIdAndMemberId(anyLong(), anyLong())).willReturn(Optional.of(memberTeam));
        given(memberRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(member));

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> teamService.resign(1L, 1L));
        assertThat(exception.getStatus()).isEqualTo(INVALID_RESIGN_STATUS);
    }

    @Test
    public void resignExceptionByMemberNotFoundInTeam() {
        // given
        Member host = createMember();
        Team team = createTeam(host);

        given(teamRepository.findByIdAndStatus(anyLong(), any())).willReturn(Optional.of(team));
        given(memberTeamRepository.findByTeamIdAndMemberId(anyLong(), anyLong())).willReturn(Optional.empty());

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> teamService.resign(1L, 1L));
        assertThat(exception.getStatus()).isEqualTo(MEMBER_NOT_FOUND_IN_TEAM);
    }

    @Test
    public void resignExceptionByHostCannotResign() {
        // given
        Member host = createMember();
        Team team = createTeam(host);
        MemberTeam memberTeam = createMemberTeam(host, team);

        given(teamRepository.findByIdAndStatus(anyLong(), any())).willReturn(Optional.of(team));
        given(memberTeamRepository.findByTeamIdAndMemberId(anyLong(), anyLong())).willReturn(Optional.of(memberTeam));
        given(memberRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(host));

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> teamService.resign(1L, 1L));
        assertThat(exception.getStatus()).isEqualTo(INVALID_RESIGN);
    }

    @Test
    public void resignExceptionByWaitResignStatus() {
        // given
        Member host = createMember();
        Member currentLoginMember = createMember();
        Team team = createTeam(host);
        MemberTeam memberTeam = createWaitMemberTeam(host, team);

        given(teamRepository.findByIdAndStatus(anyLong(), any())).willReturn(Optional.of(team));
        given(memberTeamRepository.findByTeamIdAndMemberId(anyLong(), anyLong())).willReturn(Optional.of(memberTeam));
        given(memberRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(currentLoginMember));

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> teamService.resign(1L, 1L));
        assertThat(exception.getStatus()).isEqualTo(INVALID_RESIGN_STATUS);
    }

    @Test
    public void resignExceptionByDeleteResignStatus() {
        // given
        Member host = createMember();
        Member currentLoginMember = createMember();
        Team team = createTeam(host);
        MemberTeam memberTeam = createDeleteMemberTeam(host, team);

        given(teamRepository.findByIdAndStatus(anyLong(), any())).willReturn(Optional.of(team));
        given(memberTeamRepository.findByTeamIdAndMemberId(anyLong(), anyLong())).willReturn(Optional.of(memberTeam));
        given(memberRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(currentLoginMember));

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> teamService.resign(1L, 1L));
        assertThat(exception.getStatus()).isEqualTo(INVALID_RESIGN_STATUS);
    }

    @Test
    public void createPlan() {
        // given
        Member member = createMember();
        Team team = createTeam(member);
        MemberTeam memberTeam = createDeleteMemberTeam(member, team);
        Plan plan = createCompletedPlan(member, team, LocalDate.now());
        TeamPlanRequest request = new TeamPlanRequest(1L, "title", LocalDate.now());

        given(memberFacade.getCurrentMember()).willReturn(member);
        given(memberRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(member));
        given(memberTeamRepository.findByTeamIdAndMemberId(any(), any())).willReturn(Optional.of(memberTeam));
        given(teamRepository.findByIdAndStatus(anyLong(), any())).willReturn(Optional.of(team));
        given(planRepository.save(any())).willReturn(plan);

        // when
        TeamPlanResponse response = teamService.createPlan(1L, request);

        // then
        assertThat(response.getStatus()).isEqualTo(ACTIVE);
    }

    @Test
    public void createPlanExceptionByMemberNotFoundInTeam() {
        Member member = createMember();
        TeamPlanRequest request = new TeamPlanRequest(1L, "title", LocalDate.now());

        given(memberRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(member));
        given(memberTeamRepository.findByTeamIdAndMemberId(any(), any())).willReturn(Optional.empty());

        BaseException exception = assertThrows(BaseException.class, () -> teamService.createPlan(1L, request));
        assertThat(exception.getStatus()).isEqualTo(MEMBER_NOT_FOUND_IN_TEAM);
    }

    @Test
    public void deletePlan() {
        // given
        Member manager = createMember();
        Team team = createTeam(manager);
        MemberTeam memberTeam = createMemberTeam(manager, team);
        Plan plan = createInCompletedPlan(manager, team, LocalDate.now());

        given(memberFacade.getCurrentMember()).willReturn(manager);
        given(memberRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(manager));
        given(memberTeamRepository.findByTeamIdAndMemberId(any(), any())).willReturn(Optional.of(memberTeam));
        given(planRepository.findById(anyLong())).willReturn(Optional.of(plan));

        // when
        TeamPlanResponse response = teamService.deletePlan(1L, 1L);

        // then
        assertThat(response.getStatus()).isEqualTo(DELETE);
    }

    @Test
    public void deletePlanExceptionByPlanNotFound() {
        // given
        Member manager = createMember();
        Team team = createTeam(manager);
        MemberTeam memberTeam = createMemberTeam(manager, team);

        given(memberFacade.getCurrentMember()).willReturn(manager);
        given(memberRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(manager));
        given(memberTeamRepository.findByTeamIdAndMemberId(any(), any())).willReturn(Optional.of(memberTeam));
        given(planRepository.findById(anyLong())).willReturn(Optional.empty());

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> teamService.deletePlan(1L, 1L));
        assertThat(exception.getStatus()).isEqualTo(PLAN_NOT_FOUND);
    }

    @Test
    public void deletePlanExceptionByMemberNotFound() {
        // given
        Member manager = createMember();
        given(memberFacade.getCurrentMember()).willReturn(manager);
        given(memberRepository.findByIdAndStatus(any(), any())).willReturn(Optional.empty());

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> teamService.deletePlan(1L, 1L));
        assertThat(exception.getStatus()).isEqualTo(MEMBER_NOT_FOUND);
    }

    @Test
    public void deletePlanExceptionByMemberNotFoundInTeam() {
        Member manager = createMember();
        Team team = createTeam(manager);

        given(memberFacade.getCurrentMember()).willReturn(manager);
        given(memberRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(manager));
        given(memberTeamRepository.findByTeamIdAndMemberId(any(), any())).willReturn(Optional.empty());

        BaseException exception = assertThrows(BaseException.class, () -> teamService.deletePlan(1L, 1L));
        assertThat(exception.getStatus()).isEqualTo(MEMBER_NOT_FOUND_IN_TEAM);
    }

    @Test
    public void passPlan() {
        // given
        Member manager = createMember();
        Member mandator = createMember();
        Team team = createTeam(manager);
        TeamPlanPassRequest request = new TeamPlanPassRequest(1L, 2L);

        given(planRepository.findById(anyLong())).willReturn(Optional.of(createInCompletedPlan(manager, team, LocalDate.now())));
        given(memberRepository.findByIdAndStatus(anyLong(), any())).willReturn(Optional.of(mandator));
        given(memberTeamRepository.findByTeamIdAndMemberId(any(), any())).willReturn(Optional.of(createMemberTeam(mandator, team)));
        given(memberFacade.getCurrentMember()).willReturn(manager);

        // when
        TeamPlanResponse response = teamService.passPlan(1L, 1L, request);

        // then
        verify(planRepository).findById(anyLong());
    }

    @Test
    public void passPlanExceptionByMemberNotFoundInTeam() {
        // given
        Member manager = createMember();
        Team team = createTeam(manager);
        TeamPlanPassRequest request = new TeamPlanPassRequest(1L, 2L);

        given(memberTeamRepository.findByTeamIdAndMemberId(any(), any())).willReturn(Optional.empty());

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> teamService.passPlan(1L, 1L, request));
        assertThat(exception.getStatus()).isEqualTo(MEMBER_NOT_FOUND_IN_TEAM);
    }

    @Test
    public void passPlanExceptionByMandatorNotFound() {
        // given
        Member manager = createMember();
        Member mandator = createMember();
        Team team = createTeam(manager);

        TeamPlanPassRequest request = new TeamPlanPassRequest(1L, 2L);
        MemberTeam memberTeamForMandator = createMemberTeam(mandator, team);

        given(memberTeamRepository.findByTeamIdAndMemberId(any(), any())).willReturn(Optional.of(memberTeamForMandator));
        given(memberRepository.findByIdAndStatus(any(), any())).willReturn(Optional.empty());

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> teamService.passPlan(1L, 1L, request));
        assertThat(exception.getStatus()).isEqualTo(MEMBER_NOT_FOUND);
    }

    @Test
    public void passPlanExceptionByMandatorNotFoundInTeam() {
        // given
        Member manager = createMember();
        Member mandator = createMember();
        Team team = createTeam(manager);

        TeamPlanPassRequest request = new TeamPlanPassRequest(1L, 2L);
        MemberTeam memberTeamForMandator = createMemberTeam(mandator, team);

        given(memberTeamRepository.findByTeamIdAndMemberId(any(), any())).willReturn(Optional.of(memberTeamForMandator)).willReturn(Optional.empty());
        given(memberRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(mandator));

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> teamService.passPlan(1L, 1L, request));
        assertThat(exception.getStatus()).isEqualTo(MEMBER_NOT_FOUND_IN_TEAM);
    }

    @Test
    public void passPlanExceptionPlanNotFound() {
        // given
        Member manager = createMember();
        Member mandator = createMember();
        Team team = createTeam(manager);

        TeamPlanPassRequest request = new TeamPlanPassRequest(1L, 2L);
        MemberTeam memberTeamForManager = createMemberTeam(mandator, team);
        MemberTeam memberTeamForMandator = createMemberTeam(mandator, team);


        given(memberTeamRepository.findByTeamIdAndMemberId(any(), any())).willReturn(Optional.of(memberTeamForManager)).willReturn(Optional.of(memberTeamForMandator));
        given(memberRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(mandator));
        given(planRepository.findById(any())).willReturn(Optional.empty());

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> teamService.passPlan(1L, 1L, request));
        assertThat(exception.getStatus()).isEqualTo(PLAN_NOT_FOUND);
    }

    @Test
    public void passPlanExceptionByInvalidManager() {
        // given
        Member manager = createMember();
        Member mandator = createMember();
        Team team = createTeam(manager);
        Plan plan = createInCompletedPlan(manager, team, LocalDate.now());

        TeamPlanPassRequest request = new TeamPlanPassRequest(1L, 2L);
        MemberTeam memberTeamForManager = createMemberTeam(mandator, team);
        MemberTeam memberTeamForMandator = createMemberTeam(mandator, team);

        given(memberTeamRepository.findByTeamIdAndMemberId(any(), any())).willReturn(Optional.of(memberTeamForManager)).willReturn(Optional.of(memberTeamForMandator));
        given(memberRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(mandator));
        given(planRepository.findById(any())).willReturn(Optional.of(plan));
        given(memberFacade.getCurrentMember()).willReturn(mandator);

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> teamService.passPlan(1L, 1L, request));
        assertThat(exception.getStatus()).isEqualTo(INVALID_MANAGER);
    }

    @Test
    public void passPlanExceptionByDeletedPlan() {
        // given
        Member manager = createMember();
        Member mandator = createMember();
        Team team = createTeam(manager);
        Plan plan = createDeletedPlan(manager, team, LocalDate.now());

        TeamPlanPassRequest request = new TeamPlanPassRequest(1L, 2L);
        MemberTeam memberTeamForManager = createMemberTeam(mandator, team);
        MemberTeam memberTeamForMandator = createMemberTeam(mandator, team);


        given(memberTeamRepository.findByTeamIdAndMemberId(any(), any())).willReturn(Optional.of(memberTeamForManager)).willReturn(Optional.of(memberTeamForMandator));
        given(memberRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(mandator));
        given(planRepository.findById(any())).willReturn(Optional.of(plan));
        given(memberFacade.getCurrentMember()).willReturn(manager);

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> teamService.passPlan(1L, 1L, request));
        assertThat(exception.getStatus()).isEqualTo(INVALID_PLAN);
    }

    @Test
    public void completePlan() {
        // given
        Member manager = createMember();
        Team team = createTeam(manager);
        MemberTeam memberTeam = createMemberTeam(manager, team);
        Plan plan = createInCompletedPlan(manager, team, LocalDate.now());

        given(memberTeamRepository.findByTeamIdAndMemberId(any(), any())).willReturn(Optional.of(memberTeam));
        given(planRepository.findByIdAndTeamIdAndStatus(any(), any(), any())).willReturn(Optional.of(plan));
        given(memberFacade.getCurrentMember()).willReturn(manager);

        // when
        StatusCode code = teamService.completePlan(1L, 1L, 1L);

        // then
        assertThat(code).isEqualTo(SUCCESS_COMPLETE_PLAN);
    }

    @Test
    public void completePlanExceptionByMemberNotFoundInTeam() {
        // given
        Member member = createMember();
        Team team = createTeam(member);
        MemberTeam memberTeam = createMemberTeam(member, team);

        given(memberTeamRepository.findByTeamIdAndMemberId(any(), any())).willReturn(Optional.empty());

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> teamService.completePlan(1L, 1L, 1L));
        assertThat(exception.getStatus()).isEqualTo(MEMBER_NOT_FOUND_IN_TEAM);
    }

    @Test
    public void completePlanExceptionByPlanNotFound() {
        // given
        Member member = createMember();
        Team team = createTeam(member);
        MemberTeam memberTeam = createMemberTeam(member, team);

        given(memberTeamRepository.findByTeamIdAndMemberId(any(), any())).willReturn(Optional.of(memberTeam));
        given(planRepository.findByIdAndTeamIdAndStatus(any(), any(), any())).willReturn(Optional.empty());

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> teamService.completePlan(1L, 1L, 1L));
        assertThat(exception.getStatus()).isEqualTo(PLAN_NOT_FOUND);
    }

    @Test
    public void completePlanExceptionByInvalidComplete() {
        // given
        Member manager = createMember();
        Member noManager = createMember();
        Team team = createTeam(manager);
        MemberTeam memberTeam = createMemberTeam(manager, team);
        Plan plan = createInCompletedPlan(manager, team, LocalDate.now());

        given(memberTeamRepository.findByTeamIdAndMemberId(any(), any())).willReturn(Optional.of(memberTeam));
        given(planRepository.findByIdAndTeamIdAndStatus(any(), any(), any())).willReturn(Optional.of(plan));
        given(memberFacade.getCurrentMember()).willReturn(noManager);

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> teamService.completePlan(1L, 1L, 1L));
        assertThat(exception.getStatus()).isEqualTo(INVALID_COMPLETE_PLAN);
    }

    @Test
    public void incompletePlan() {
        // given
        Member member = createMember();
        Team team = createTeam(member);
        MemberTeam memberTeam = createMemberTeam(member, team);
        Plan plan = createCompletedPlan(member, team, LocalDate.now());

        given(memberTeamRepository.findByTeamIdAndMemberId(any(), any())).willReturn(Optional.of(memberTeam));
        given(planRepository.findByIdAndTeamIdAndStatus(any(), any(), any())).willReturn(Optional.of(plan));

        // when
        StatusCode code = teamService.incompletePlan(1L, 1L, 1L);

        // then
        assertThat(code).isEqualTo(SUCCESS_INCOMPLETE_PLAN);

    }

    @Test
    public void incompletePlanExceptionByMemberNotFoundInTeam() {
        // given
        given(memberTeamRepository.findByTeamIdAndMemberId(any(), any())).willReturn(Optional.empty());

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> teamService.incompletePlan(1L, 1L, 1L));
        assertThat(exception.getStatus()).isEqualTo(MEMBER_NOT_FOUND_IN_TEAM);
    }

    @Test
    public void incompletePlanExceptionByPlanNotFound() {
        // given
        Member member = createMember();
        Team team = createTeam(member);
        MemberTeam memberTeam = createMemberTeam(member, team);

        given(memberTeamRepository.findByTeamIdAndMemberId(any(), any())).willReturn(Optional.of(memberTeam));
        given(planRepository.findByIdAndTeamIdAndStatus(any(), any(), any())).willReturn(Optional.empty());

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> teamService.completePlan(1L, 1L, 1L));
        assertThat(exception.getStatus()).isEqualTo(PLAN_NOT_FOUND);
    }

    @Test
    public void incompletePlanExceptionByAlreadyIncomplete() {
        // given
        Member member = createMember();
        Team team = createTeam(member);
        MemberTeam memberTeam = createMemberTeam(member, team);
        Plan plan = createInCompletedPlan(member, team, LocalDate.now());

        given(memberTeamRepository.findByTeamIdAndMemberId(any(), any())).willReturn(Optional.of(memberTeam));
        given(planRepository.findByIdAndTeamIdAndStatus(any(), any(), any())).willReturn(Optional.of(plan));

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> teamService.incompletePlan(1L, 1L, 1L));
        assertThat(exception.getStatus()).isEqualTo(ALREADY_INCOMPLETE_PLAN);
    }

    @Test
    public void getTeamCalendarByDate() {

    }

    @Test
    public void getTeamCalendarByDateExceptionByTeamNotFound() {
        // given
        given(teamRepository.findByIdAndStatus(any(), any())).willReturn(Optional.empty());

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> teamService.getTeamCalendarByDate(1L, LocalDate.now(), 1L));
        assertThat(exception.getStatus()).isEqualTo(TEAM_NOT_FOUND);
    }
}
