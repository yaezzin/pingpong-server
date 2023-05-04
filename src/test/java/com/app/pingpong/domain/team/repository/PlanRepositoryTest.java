package com.app.pingpong.domain.team.repository;

import com.app.pingpong.domain.member.entity.Member;
import com.app.pingpong.domain.member.repository.MemberRepository;
import com.app.pingpong.domain.team.entity.Plan;
import com.app.pingpong.domain.team.entity.Team;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.Optional;

import static com.app.pingpong.factory.MemberFactory.createMember;
import static com.app.pingpong.factory.PlanFactory.createInCompletedPlan;
import static com.app.pingpong.factory.TeamFactory.createTeam;
import static com.app.pingpong.global.common.status.Status.ACTIVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
public class PlanRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    PlanRepository planRepository;

    @Autowired
    TeamRepository teamRepository;

    @Test
    public void findByIdAndStatus() {
        // given
        Member member = memberRepository.save(createMember());
        Team team = teamRepository.save(createTeam(member));
        Plan plan = planRepository.save(createInCompletedPlan(member, team, LocalDate.now()));

        // when
        Optional<Plan> response = planRepository.findByIdAndStatus(plan.getId(), ACTIVE);

        // then
        assertTrue(response.isPresent());
        assertThat(plan.getId()).isEqualTo(response.get().getId());
    }

    @Test
    public void findByIdAndTeamIdAndStatus() {
        // given
        Member member = memberRepository.save(createMember());
        Team team = teamRepository.save(createTeam(member));
        Plan plan = planRepository.save(createInCompletedPlan(member, team, LocalDate.now()));

        // when
        Optional<Plan> response = planRepository.findByIdAndTeamIdAndStatus(plan.getId(), team.getId(), ACTIVE);

        // then
        assertTrue(response.isPresent());
        assertThat(plan.getId()).isEqualTo(response.get().getId());
        assertThat(plan.getStatus()).isEqualTo(response.get().getStatus());

    }

}
