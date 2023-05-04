package com.app.pingpong.domain.team.service;

import com.app.pingpong.domain.friend.repository.FriendFactory;
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
import com.app.pingpong.global.common.util.MemberFacade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
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
public class TeamServiceTest {

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
        given(teamRepository.findAllByHostId(any())).willReturn(exceedTeamList);

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
        assertThat(memberTeamForMember.getStatus()).isEqualTo(DELETE);
    }

    @Test
    public void resignExceptionByTeamNotFound() {
        // given
        given(teamRepository.findByIdAndStatus(anyLong(), any())).willReturn(Optional.empty());

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> teamService.resign(1L, 1L));
        assertThat(exception.getStatus()).isEqualTo(TEAM_NOT_FOUND);
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
    public void createPlanExceptionByManagerNotFound() {
        // given
        TeamPlanRequest request = new TeamPlanRequest(1L, "title", LocalDate.now());
        given(memberRepository.findByIdAndStatus(any(), any())).willReturn(Optional.empty());

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> teamService.createPlan(1L, request));
        assertThat(exception.getStatus()).isEqualTo(MEMBER_NOT_FOUND);
    }

    @Test
    public void createPlanExceptionByManagerNotFoundInTeam() {
        // given
        Member manager = createMember();
        TeamPlanRequest request = new TeamPlanRequest(1L, "title", LocalDate.now());

        given(memberRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(manager));
        given(memberTeamRepository.findByTeamIdAndMemberId(any(), any())).willReturn(Optional.empty());

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> teamService.createPlan(1L, request));
        assertThat(exception.getStatus()).isEqualTo(MEMBER_NOT_FOUND_IN_TEAM);
    }

    @Test
    public void createPlanExceptionByMakerNotFound() {
        // given
        Member manager = createMember();
        Member maker = createMember();
        Team team = createTeam(manager);
        MemberTeam memberTeamForManager = createMemberTeam(manager, team);
        TeamPlanRequest request = new TeamPlanRequest(1L, "title", LocalDate.now());

        given(memberFacade.getCurrentMember()).willReturn(maker);
        given(memberRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(manager)).willReturn(Optional.empty());
        given(memberTeamRepository.findByTeamIdAndMemberId(any(), any())).willReturn(Optional.of(memberTeamForManager));

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> teamService.createPlan(1L, request));
        assertThat(exception.getStatus()).isEqualTo(MEMBER_NOT_FOUND);
    }

    @Test
    public void createPlanExceptionByMakerNotFoundInTeam() {
        // given
        Member manager = createMember();
        Member maker = createMember();
        Team team = createTeam(manager);
        MemberTeam memberTeamForManager = createMemberTeam(manager, team);
        TeamPlanRequest request = new TeamPlanRequest(1L, "title", LocalDate.now());

        given(memberFacade.getCurrentMember()).willReturn(maker);
        given(memberRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(manager)).willReturn(Optional.of(maker));
        given(memberTeamRepository.findByTeamIdAndMemberId(any(), any())).willReturn(Optional.of(memberTeamForManager)).willReturn(Optional.empty());

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> teamService.createPlan(1L, request));
        assertThat(exception.getStatus()).isEqualTo(MEMBER_NOT_FOUND_IN_TEAM);
        verify(memberRepository, times(2)).findByIdAndStatus(any(), any());
        verify(memberTeamRepository, times(2)).findByTeamIdAndMemberId(any(), any());
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
        BaseException exception = assertThrows(BaseException.class, () -> teamService.incompletePlan(1L, 1L, 1L));
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
        // given
        Member loginMember = createMember();
        Team team = createTeam(loginMember);
        MemberTeam memberTeam = createMemberTeam(loginMember, team);
        LocalDate date = LocalDate.of(2023, 5, 4);
        List<Plan> plans = createPlanList(loginMember, team, date);

        given(teamRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(team));
        given(memberTeamRepository.findByTeamIdAndMemberId(any(), any())).willReturn(Optional.of(memberTeam));
        given(planRepository.findAllByTeamIdAndDateAndStatus(any(), any(), any())).willReturn(plans);

        // when
        TeamPlanDetailResponse response = teamService.getTeamCalendarByDate(1L, date, 1L);

        // then
        assertThat(response.getPlanList().size()).isEqualTo(plans.size());
    }

    @Test
    public void getTeamCalendarByDateExceptionByTeamNotFound() {
        // given
        given(teamRepository.findByIdAndStatus(any(), any())).willReturn(Optional.empty());

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> teamService.getTeamCalendarByDate(1L, LocalDate.now(), 1L));
        assertThat(exception.getStatus()).isEqualTo(TEAM_NOT_FOUND);
    }

    @Test
    public void getTeamCalendarByDateExceptionByMemberNotFoundInTeam() {
        // given
        Member loginMember = createMember();
        Team team = createTeam(loginMember);

        given(teamRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(team));
        given(memberTeamRepository.findByTeamIdAndMemberId(any(), any())).willReturn(Optional.empty());

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> teamService.getTeamCalendarByDate(1L, LocalDate.now(), 1L));
        assertThat(exception.getStatus()).isEqualTo(MEMBER_NOT_FOUND_IN_TEAM);
    }

    @Test
    public void getTeamAchievementRate() {
        // given
        Member member = createMember();
        Team team = createTeam(member);

        LocalDate startDate = LocalDate.of(2023, 4, 1);
        LocalDate endDate = LocalDate.of(2023, 4, 30);
        TeamAchieveRequest request = new TeamAchieveRequest(startDate, endDate);

        List<Plan> completedPlans = createCompletedPlansByCount(member, team, startDate, 10);
        List<Plan> incompletedPlans = createInCompletedPlansByCount(member, team, startDate, 10);

        List<Plan> allPlans = new ArrayList<>();
        allPlans.addAll(completedPlans);
        allPlans.addAll(incompletedPlans);

        given(planRepository.findAllByTeamIdAndStatusAndDateBetween(any(), any(), any(), any())).willReturn(allPlans);
        given(planRepository.findAllByTeamIdAndStatusAndDate(any(), any(), any())).willReturn(allPlans);

        // when
        List<TeamAchieveResponse> response = teamService.getTeamAchievementRate(1L, request);

        // then
        double rate = (double) completedPlans.size() / (completedPlans.size() + incompletedPlans.size()) * 100;
        assertThat(response.get(0).getAchievement()).isEqualTo(rate);
    }

    @Test
    @DisplayName("Host가 조회 시 휴지통의 모든 삭제된 일정이 조회")
    public void getTrashByHost() {
        // given
        Member host = createMember();
        Team team = createTeam(host);
        MemberTeam memberTeam = createMemberTeam(host, team);
        List<Plan> plans = createDeletedPlanList(host, team, LocalDate.now());

        given(teamRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(team));
        given(memberRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(host));
        given(memberTeamRepository.findByTeamIdAndMemberId(any(), any())).willReturn(Optional.of(memberTeam));
        given(planRepository.findAllByTeamIdAndStatusOrderByWastedTimeDesc(any(), any())).willReturn(plans);

        // when
        List<TeamPlanResponse> trash = teamService.getTrash(1L, 1L);

        // then
        assertThat(trash.size()).isEqualTo(plans.size());
    }

    @Test
    @DisplayName("Host가 아닌 '일정 담당자'의 조회 시 자신의 삭제된 일정만 조회")
    public void getTrashByManager() {
        // given
        Member host = createMember();
        Member manager = createMember();
        Team team = createTeam(host);
        MemberTeam memberTeam = createMemberTeam(host, team);

        List<Plan> plansForHost = createDeletedPlanList(host, team, LocalDate.now());
        List<Plan> plansForManager = createDeletedPlanList(manager, team, LocalDate.now());
        List<Plan> allPlans = new ArrayList<>();
        allPlans.addAll(plansForHost);
        allPlans.addAll(plansForManager);

        given(teamRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(team));
        given(memberRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(manager));
        given(memberTeamRepository.findByTeamIdAndMemberId(any(), any())).willReturn(Optional.of(memberTeam));
        given(planRepository.findAllByManagerIdAndTeamIdAndStatusOrderByWastedTimeDesc(any(), any(), any())).willReturn(plansForManager);

        // when
        List<TeamPlanResponse> trash = teamService.getTrash(1L, 1L);

        // then
        assertThat(trash.size()).isEqualTo(plansForManager.size());
        assertThat(trash.size()).isEqualTo(allPlans.size() - plansForHost.size());
    }

    @Test
    public void getTrashExceptionByTeamNotFound() {
        // given
        given(teamRepository.findByIdAndStatus(any(), any())).willReturn(Optional.empty());

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> teamService.getTrash(1L, 1L));
        assertThat(exception.getStatus()).isEqualTo(TEAM_NOT_FOUND);
    }

    @Test
    public void getTrashExceptionByMemberNotFound() {
        // given
        Member member = createMember();
        Team team = createTeam(member);

        given(teamRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(team));
        given(memberRepository.findByIdAndStatus(any(), any())).willReturn(Optional.empty());

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> teamService.getTrash(1L, 1L));
        assertThat(exception.getStatus()).isEqualTo(MEMBER_NOT_FOUND);
    }

    @Test
    public void getTrashExceptionByMemberNotInTeam() {
        // given
        Member member = createMember();
        Team team = createTeam(member);

        given(teamRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(team));
        given(memberRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(member));
        given(memberTeamRepository.findByTeamIdAndMemberId(any(), any())).willReturn(Optional.empty());

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> teamService.getTrash(1L, 1L));
        assertThat(exception.getStatus()).isEqualTo(MEMBER_NOT_FOUND_IN_TEAM);
    }

    @Test
    public void deleteAllTrash() {
        // given
        Member host = createMember();
        Team team = createTeam(host);
        MemberTeam memberTeam = createMemberTeam(host, team);
        List<Plan> plans = createPlanList(host, team, LocalDate.now());

        given(memberRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(host));
        given(teamRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(team));
        given(memberTeamRepository.findByTeamIdAndMemberId(any(), any())).willReturn(Optional.of(memberTeam));
        given(planRepository.findAllByTeamIdAndStatusOrderByWastedTimeDesc(any(), any())).willReturn(plans);

        // when
        StatusCode code = teamService.deleteAllTrash(1L, 1L);

        // then
        assertThat(code).isEqualTo(SUCCESS_DELETE_ALL_TRASH);
        plans.forEach(plan -> {
            assertThat(plan.getStatus()).isEqualTo(PERMANENT);
        });
    }

    @Test
    public void deleteAllTrashExceptionByMemberNotFound() {
        // given
        given(memberRepository.findByIdAndStatus(any(), any())).willReturn(Optional.empty());

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> teamService.deleteAllTrash(1L, 1L));
        assertThat(exception.getStatus()).isEqualTo(MEMBER_NOT_FOUND);
    }

    @Test
    public void deleteAllTrashExceptionByTeamNotFound() {
        // given
        Member member = createMember();
        given(memberRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(member));
        given(teamRepository.findByIdAndStatus(any(), any())).willReturn(Optional.empty());

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> teamService.deleteAllTrash(1L, 1L));
        assertThat(exception.getStatus()).isEqualTo(TEAM_NOT_FOUND);
    }

    @Test
    public void deleteAllTrashExceptionByMemberNotFoundInTeam() {
        // given
        Member member = createMember();
        Team team = createTeam(member);

        given(memberRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(member));
        given(teamRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(team));
        given(memberTeamRepository.findByTeamIdAndMemberId(any(), any())).willReturn(Optional.empty());

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> teamService.deleteAllTrash(1L, 1L));
        assertThat(exception.getStatus()).isEqualTo(MEMBER_NOT_FOUND_IN_TEAM);
    }

    @Test
    public void deleteAllTrashExceptionByInvalidHost() {
        // given
        Member host = createMember();
        Member noHost = createMember();
        Team team = createTeam(host);
        MemberTeam memberTeamForNoHost = createMemberTeam(noHost, team);

        given(memberRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(noHost));
        given(teamRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(team));
        given(memberTeamRepository.findByTeamIdAndMemberId(any(), any())).willReturn(Optional.of(memberTeamForNoHost));

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> teamService.deleteAllTrash(1L, 1L));
        assertThat(exception.getStatus()).isEqualTo(INVALID_HOST);
    }

    @Test
    public void deleteTrash() {
        // given
        Member host = createMember();
        Team team = createTeam(host);
        MemberTeam memberTeam = createMemberTeam(host, team);
        Plan plan = createDeletedPlan(host, team, LocalDate.now());

        given(memberRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(host));
        given(teamRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(team));
        given(memberTeamRepository.findByTeamIdAndMemberId(any(), any())).willReturn(Optional.of(memberTeam));
        given(planRepository.findById(any())).willReturn(Optional.of(plan));

        // when
        StatusCode code = teamService.deleteTrash(1L, 1L, 1L);

        // then
        assertThat(code).isEqualTo(SUCCESS_DELETE_TRASH);
        assertThat(plan.getStatus()).isEqualTo(PERMANENT);
    }

    @Test
    public void deleteTrashExceptionByMemberNotFound() {
        // given
        given(memberRepository.findByIdAndStatus(any(), any())).willReturn(Optional.empty());

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> teamService.deleteTrash(1L, 1L, 1L));
        assertThat(exception.getStatus()).isEqualTo(MEMBER_NOT_FOUND);
    }

    @Test
    public void deleteTrashExceptionByTeamNotFound() {
        // given
        Member member = createMember();
        given(memberRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(member));
        given(teamRepository.findByIdAndStatus(any(), any())).willReturn(Optional.empty());

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> teamService.deleteTrash(1L, 1L, 1L));
        assertThat(exception.getStatus()).isEqualTo(TEAM_NOT_FOUND);
    }

    @Test
    public void deleteTrashExceptionByMemberNotFoundInTeam() {
        // given
        Member member = createMember();
        Team team = createTeam(member);

        given(memberRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(member));
        given(teamRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(team));
        given(memberTeamRepository.findByTeamIdAndMemberId(any(), any())).willReturn(Optional.empty());

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> teamService.deleteTrash(1L, 1L, 1L));
        assertThat(exception.getStatus()).isEqualTo(MEMBER_NOT_FOUND_IN_TEAM);
    }

    @Test
    public void deleteTrashExceptionByInvalidHost() {
        // given
        Member host = createMember();
        Member noHost = createMember();
        Team team = createTeam(host);
        MemberTeam memberTeamForNoHost = createMemberTeam(noHost, team);

        given(memberRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(noHost));
        given(teamRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(team));
        given(memberTeamRepository.findByTeamIdAndMemberId(any(), any())).willReturn(Optional.of(memberTeamForNoHost));

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> teamService.deleteTrash(1L, 1L, 1L));
        assertThat(exception.getStatus()).isEqualTo(INVALID_HOST);
    }

    @Test
    public void deleteTrashExceptionByPlanNotFound() {
        // given
        Member host = createMember();
        Team team = createTeam(host);
        MemberTeam memberTeam = createMemberTeam(host, team);

        given(memberRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(host));
        given(teamRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(team));
        given(memberTeamRepository.findByTeamIdAndMemberId(any(), any())).willReturn(Optional.of(memberTeam));
        given(planRepository.findById(any())).willReturn(Optional.empty());

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> teamService.deleteTrash(1L, 1L, 1L));
        assertThat(exception.getStatus()).isEqualTo(PLAN_NOT_FOUND);
    }

    @Test
    public void recoverTrash() {
        // given
        Member member = createMember();
        Team team = createTeam(member);
        MemberTeam memberTeam = createMemberTeam(member, team);
        Plan plan = createDeletedPlan(member, team, LocalDate.now());

        given(teamRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(team));
        given(memberTeamRepository.findByTeamIdAndMemberId(any(), any())).willReturn(Optional.of(memberTeam));
        given(planRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(plan));

        // when
        StatusCode code = teamService.recoverTrash(1L, 1L, 1L);

        // then
        assertThat(code).isEqualTo(SUCCESS_RECOVER_TRASH);
        assertThat(plan.getStatus()).isEqualTo(ACTIVE);
    }

    @Test
    public void recoverTrashExceptionByTeamNotFound() {
        // given
        given(teamRepository.findByIdAndStatus(any(), any())).willReturn(Optional.empty());

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> teamService.recoverTrash(1L, 1L, 1L));
        assertThat(exception.getStatus()).isEqualTo(TEAM_NOT_FOUND);
    }

    @Test
    public void recoverTrashExceptionByMemberNotFoundInTeam() {
        // given
        Member member = createMember();
        Team team = createTeam(member);

        given(teamRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(team));
        given(memberTeamRepository.findByTeamIdAndMemberId(any(), any())).willReturn(Optional.empty());


        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> teamService.recoverTrash(1L, 1L, 1L));
        assertThat(exception.getStatus()).isEqualTo(MEMBER_NOT_FOUND_IN_TEAM);
    }

    @Test
    public void recoverTrashExceptionByPlanNotFound() {
        // given
        Member member = createMember();
        Team team = createTeam(member);
        MemberTeam memberTeam = createMemberTeam(member, team);

        given(teamRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(team));
        given(memberTeamRepository.findByTeamIdAndMemberId(any(), any())).willReturn(Optional.of(memberTeam));
        given(planRepository.findByIdAndStatus(any(), any())).willReturn(Optional.empty());

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> teamService.recoverTrash(1L, 1L, 1L));
        assertThat(exception.getStatus()).isEqualTo(PLAN_NOT_FOUND);
    }
}
