package com.app.pingpong.domain.team.service;

import com.app.pingpong.domain.member.entity.Member;
import com.app.pingpong.domain.member.entity.MemberTeam;
import com.app.pingpong.domain.member.repository.MemberTeamRepository;
import com.app.pingpong.domain.team.entity.Team;
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

import java.util.Optional;

import static com.app.pingpong.factory.MemberFactory.createMember;
import static com.app.pingpong.factory.MemberTeamFactory.*;
import static com.app.pingpong.factory.TeamFactory.createTeam;
import static com.app.pingpong.global.common.exception.StatusCode.*;
import static com.app.pingpong.global.common.status.Status.ACTIVE;
import static com.app.pingpong.global.common.status.Status.WAIT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class TeamServiceMockTest {

    @InjectMocks
    TeamService teamService;

    @Mock
    TeamRepository teamRepository;

    @Mock
    MemberTeamRepository memberTeamRepository;

    @Mock
    MemberFacade memberFacade;

    @Test
    @DisplayName("팀 수락 성공")
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
        StatusCode result = teamService.accept(1L);

        // then
        assertEquals(result, SUCCESS_ACCEPT_TEAM_INVITATION);
    }

    @Test
    @DisplayName("팀 거절 성공 테스트")
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
    @DisplayName("팀 거절 실패 테스트")
    public void refuse_fail() {
        // given
        Member member = createMember();
        Team team = createTeam(member);
        MemberTeam memberTeam = createWaitMemberTeam(member, team);

        given(memberFacade.getCurrentMember()).willReturn(member);
        given(teamRepository.findByIdAndStatus(anyLong(), any())).willReturn(Optional.of(team));
        given(memberTeamRepository.findByTeamIdAndMemberIdAndStatus(any(), any(), eq(WAIT))).willReturn(Optional.empty());

        BaseException exception = assertThrows(BaseException.class, () -> {
            teamService.refuse(1L);
        });
        assertThat(exception.getStatus()).isEqualTo(TEAM_INVITATION_NOT_FOUND);
    }

    @Test
    @DisplayName("탈퇴 성공 테스트")
    public void resign() {
        Member member = createMember();
        Team team = createTeam(member);
        MemberTeam memberTeam = createMemberTeam(member, team);

        given(teamRepository.findByIdAndStatus(anyLong(), any())).willReturn(Optional.of(team));
        given(memberTeamRepository.findByTeamIdAndMemberId(anyLong(), anyLong())).willReturn(Optional.of(memberTeam));

        // when
        StatusCode code = teamService.resign(1L, 1L);

        // then
        assertThat(code).isEqualTo(SUCCESS_RESIGN_TEAM);
    }

    @Test
    @DisplayName("탈퇴 실패 테스트 - 아직 팀 초대를 수락하지 않은 경우")
    public void resign_fail_not_invite() {
        // given
        Member member = createMember();
        Team team = createTeam(member);
        MemberTeam memberTeam = createWaitMemberTeam(member, team);

        given(teamRepository.findByIdAndStatus(anyLong(), any())).willReturn(Optional.of(team));
        given(memberTeamRepository.findByTeamIdAndMemberId(anyLong(), anyLong())).willReturn(Optional.of(memberTeam));

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> teamService.resign(1L, 1L));
        assertThat(exception.getStatus()).isEqualTo(INVALID_RESIGN_STATUS);
    }

    @Test
    @DisplayName("탈퇴 실패 테스트 - 이미 탈퇴한 경우")
    public void resign_fail_already_resign() {
        // given
        Member member = createMember();
        Team team = createTeam(member);
        MemberTeam memberTeam = createDeleteMemberTeam(member, team);

        given(teamRepository.findByIdAndStatus(anyLong(), any())).willReturn(Optional.of(team));
        given(memberTeamRepository.findByTeamIdAndMemberId(anyLong(), anyLong())).willReturn(Optional.of(memberTeam));

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> teamService.resign(1L, 1L));
        assertThat(exception.getStatus()).isEqualTo(INVALID_RESIGN_STATUS);
    }

    @Test
    @DisplayName("일정 생성 성공 테스트")
    public void createPlan() {

    }
}
