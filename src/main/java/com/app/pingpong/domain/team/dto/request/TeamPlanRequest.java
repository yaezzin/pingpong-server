package com.app.pingpong.domain.team.dto.request;

import com.app.pingpong.domain.team.entity.Plan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TeamPlanRequest {
    private Long managerId;
    private String title;
    private LocalDate date;

    @Builder
    public Plan toEntity() {
        return Plan.builder()
                .title(title)
                .date(date)
                .build();
    }
}
