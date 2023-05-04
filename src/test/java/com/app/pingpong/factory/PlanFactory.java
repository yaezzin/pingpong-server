package com.app.pingpong.factory;

import com.app.pingpong.domain.member.entity.Member;
import com.app.pingpong.domain.team.entity.Plan;
import com.app.pingpong.domain.team.entity.Team;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    public static Plan createDeletedPlan(Member manager, Team team, LocalDate date) {
        Plan plan = new Plan("title", date);
        plan.setManager(manager);
        plan.setTeam(team);
        plan.setStatus(DELETE);
        plan.setAchievement(INCOMPLETE);
        return plan;
    }

    public static List<Plan> createPlanList(Member manager, Team team, LocalDate date) {
        List<Plan> planList = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            Plan plan = new Plan("title", date);
            plan.setManager(manager);
            plan.setTeam(team);
            plan.setStatus(ACTIVE);
            plan.setAchievement(INCOMPLETE);
            plan.setDate(date);
            planList.add(plan);
        }
        return planList;
    }

    public static List<Plan> createDeletedPlanList(Member manager, Team team, LocalDate date) {
        List<Plan> planList = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            Plan plan = new Plan("title", date);
            plan.setManager(manager);
            plan.setTeam(team);
            plan.setStatus(DELETE);
            plan.setAchievement(INCOMPLETE);
            plan.setDate(date);
            planList.add(plan);
        }
        return planList;
    }
}
