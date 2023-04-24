package com.app.pingpong.domain.team.dto.response;

import com.app.pingpong.domain.team.entity.Plan;
import com.app.pingpong.global.common.status.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@AllArgsConstructor
public class TeamPlanResponse {
    private Long planId;
    private Long managerId;
    private String title;
    private LocalDate date;
    private Status status;
    private Status achievement;

    public static TeamPlanResponse of(Plan plan) {
        return new TeamPlanResponse(plan.getId(), plan.getManager().getId(), plan.getTitle(), plan.getDate(), plan.getStatus(), plan.getAchievement());
    }

    public static List<TeamPlanResponse> of(List<Plan> plans) {
        List<TeamPlanResponse> list = new ArrayList<>();
        for (Plan p : plans) {
            list.add(TeamPlanResponse.builder()
                    .planId(p.getId())
                    .managerId(p.getManager().getId())
                    .title(p.getTitle())
                    .date(p.getDate())
                    .status(p.getStatus())
                    .achievement(p.getAchievement())
                    .build()
            );
        }
        return list;
    }
}
