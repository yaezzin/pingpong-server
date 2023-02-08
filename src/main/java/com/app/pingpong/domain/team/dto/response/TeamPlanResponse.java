package com.app.pingpong.domain.team.dto.response;

import com.app.pingpong.domain.team.entity.Plan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Builder
@Getter
@AllArgsConstructor
public class TeamPlanResponse {
    private Long managerId;
    private String title;
    private LocalDate date;

    public static TeamPlanResponse of(Plan plan) {
        return new TeamPlanResponse(plan.getManager().getId(), plan.getTitle(), plan.getDate());
    }
}
