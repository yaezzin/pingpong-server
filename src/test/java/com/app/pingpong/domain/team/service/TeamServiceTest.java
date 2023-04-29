package com.app.pingpong.domain.team.service;

import com.app.pingpong.domain.member.entity.Member;
import com.app.pingpong.domain.member.entity.MemberTeam;
import com.app.pingpong.domain.member.repository.MemberRepository;
import com.app.pingpong.domain.member.repository.MemberTeamRepository;
import com.app.pingpong.domain.team.dto.request.TeamRequest;
import com.app.pingpong.domain.team.dto.response.TeamResponse;
import com.app.pingpong.domain.team.entity.Team;
import com.app.pingpong.domain.team.repository.TeamRepository;
import com.app.pingpong.global.common.exception.BaseException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;

import static com.app.pingpong.factory.MemberFactory.createMember;
import static com.app.pingpong.factory.MemberTeamFactory.createMemberTeam;
import static com.app.pingpong.factory.TeamFactory.createTeam;
import static com.app.pingpong.global.common.exception.StatusCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
public class TeamServiceTest {

    @Autowired
    TeamService teamService;

    @Autowired
    TeamRepository teamRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    MemberTeamRepository memberTeamRepository;

    @Test
    @DisplayName("팀 생성 성공")
    public void create() {
        Member host = memberRepository.save(createMember());
        setAuthenticatedMember(host);
        List<Long> memberList = createMultipleMembersAndReturnIds(5);

        String teamName = "createTeam";
        TeamRequest request = new TeamRequest(teamName, memberList);

        // when
        TeamResponse response = teamService.create(request);

        // then
        memberList.add(0, host.getId());
        assertThat(response.getTeamName()).isEqualTo(teamName);
        assertThat(response.getMemberId()).isEqualTo(memberList);
    }

    @Test
    @DisplayName("팀 생성 성공 실패 - 호스트의 ID가 존재")
    public void create_fail_include_host() {
        Member host = memberRepository.save(createMember());
        setAuthenticatedMember(host);

        TeamRequest request = new TeamRequest("team", List.of(host.getId()));
        BaseException exceed_exception = assertThrows(BaseException.class, () -> {
            teamService.create(request);
        });
        assertThat(exceed_exception.getStatus()).isEqualTo(INVALID_TEAM_MEMBER);
    }

    @Test
    @DisplayName("팀 생성 실패 - 그룹 멤버는 최소 1명, 최대 10명으로 제한")
    public void create_fail() {
        // given
        Member host = memberRepository.save(createMember());
        setAuthenticatedMember(host);
        List<Long> memberList = createMultipleMembersAndReturnIds(11);

        TeamRequest exceed = new TeamRequest("team", memberList);
        TeamRequest empty = new TeamRequest("team", List.of());

        // then
        BaseException exceed_exception = assertThrows(BaseException.class, () -> {
            teamService.create(exceed);
        });
        assertThat(exceed_exception.getStatus()).isEqualTo(INVALID_TEAM_MEMBER_SIZE);

        BaseException empty_exception = assertThrows(BaseException.class, () -> {
            teamService.create(empty);
        });
        assertThat(empty_exception.getStatus()).isEqualTo(INVALID_TEAM_MEMBER_SIZE);


    }

    @Test
    @DisplayName("팀 생성 실패 - 해당 유저가 호스트인 팀이 6개 이상")
    public void create_fail_exceed_host_team() {
        // given
        Member member = memberRepository.save(createMember());
        setAuthenticatedMember(member);
        createMultipleTeamsForMember(member);
        TeamRequest request = new TeamRequest("team", List.of(1L, 2L, 3L));

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> {
            teamService.create(request);
        });
        assertThat(exception.getStatus()).isEqualTo(EXCEED_HOST_TEAM_SIZE);
    }

    private void setAuthenticatedMember(Member member) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(member.getEmail(), member.getSocialId());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void createMultipleTeamsForMember(Member member) {
        for (int i = 0; i < 10; i++) {
            Team team = teamRepository.save(createTeam(member));
            MemberTeam memberTeam = memberTeamRepository.save(createMemberTeam(member, team));
        }
    }

    private List<Long> createMultipleMembersAndReturnIds(int count) {
        List<Long> memberList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Member member = memberRepository.save(createMember("email" + i + "email@com", "nickname" + i));
            memberList.add(member.getId());
        }
        return memberList;
    }
}
