package com.app.pingpong.domain.team.repository;

import com.app.pingpong.domain.member.entity.Member;
import com.app.pingpong.domain.member.repository.MemberRepository;
import com.app.pingpong.domain.team.entity.Team;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static com.app.pingpong.factory.MemberFactory.createMember;
import static com.app.pingpong.factory.TeamFactory.createDeleteTeam;
import static com.app.pingpong.factory.TeamFactory.createTeam;
import static com.app.pingpong.global.common.status.Status.ACTIVE;
import static com.app.pingpong.global.common.status.Status.DELETE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
public class TeamRepositoryTest {

    @Autowired
    TeamRepository teamRepository;

    @Autowired
    MemberRepository memberRepository;

    @Test
    public void findByHostId() {
        // given
        Member host = memberRepository.save(createMember());

        Team team1 = teamRepository.save(createTeam(host));
        Team team2 = teamRepository.save(createTeam(host));
        Team team3 = teamRepository.save(createTeam(host));
        List<Team> teamList = List.of(team1, team2, team3);

        // when
        List<Team> response = teamRepository.findAllByHostId(host.getId());

        // then
        assertThat(response.size()).isEqualTo(teamList.size());
    }

    @Test
    public void findByIdAndStatus() {
        // given
        Member host = memberRepository.save(createMember());
        Team active = teamRepository.save(createTeam(host));
        Team delete = teamRepository.save(createDeleteTeam(host));

        // when
        Optional<Team> activeResponse = teamRepository.findByIdAndStatus(active.getId(), ACTIVE);
        Optional<Team> deleteResponse = teamRepository.findByIdAndStatus(delete.getId(), DELETE);

        // then
        assertTrue(activeResponse.isPresent());
        assertThat(active.getStatus()).isEqualTo(activeResponse.get().getStatus());
        assertTrue(deleteResponse.isPresent());
        assertThat(delete.getStatus()).isEqualTo(deleteResponse.get().getStatus());
    }
}
