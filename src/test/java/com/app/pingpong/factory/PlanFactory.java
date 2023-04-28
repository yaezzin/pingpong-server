package com.app.pingpong.factory;

import com.app.pingpong.domain.member.entity.Member;
import com.app.pingpong.domain.team.entity.Plan;
import com.app.pingpong.domain.team.entity.Team;

import java.time.LocalDate;

import static com.app.pingpong.global.common.status.Status.*;

public class PlanFactory {

    public static Plan createCompletedPlan(Member manager, Team team, LocalDate date) {
        Plan plan = new Plan("title", date);
        plan.setManager(manager);
        plan.setTeam(team);
        plan.setStatus(ACTIVE);
        plan.setAchievement(COMPLETE);
        return plan;
    }

    public static Plan createInCompletedPlan(Member manager, Team team, LocalDate date) {
        Plan plan = new Plan("title", date);
        plan.setManager(manager);
        plan.setTeam(team);
        plan.setStatus(ACTIVE);
        plan.setAchievement(INCOMPLETE);
        return plan;
    }
}
