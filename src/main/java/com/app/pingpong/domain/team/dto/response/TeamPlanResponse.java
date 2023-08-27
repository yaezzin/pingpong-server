package com.app.pingpong.domain.team.dto.response;

import com.app.pingpong.domain.member.dto.response.MemberResponse;
import com.app.pingpong.domain.team.entity.Plan;
import com.app.pingpong.global.common.status.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Builder
@Getter
@AllArgsConstructor
public class TeamPlanResponse {
    private Long planId;
    private String title;
    private LocalDate date;
    private Status status;
    private Status achievement;
    private MemberResponse manager;

    public static TeamPlanResponse of(Plan plan) {
        return new TeamPlanResponse(plan.getId(), plan.getTitle(), plan.getDate(), plan.getStatus(), plan.getAchievement(), MemberResponse.of(plan.getManager()));
    }

    public static List<TeamPlanResponse> of(List<Plan> plans) {
        return plans.stream()
                .map(plan -> new TeamPlanResponse(plan.getId(),
                        plan.getTitle(),
                        plan.getDate(),
                        plan.getStatus(),
                        plan.getAchievement(),
                        MemberResponse.of(plan.getManager())
                ))
                .collect(Collectors.toList());
    }
}
