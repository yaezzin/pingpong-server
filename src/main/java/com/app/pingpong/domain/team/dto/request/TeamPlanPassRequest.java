package com.app.pingpong.domain.team.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TeamPlanPassRequest {
    private Long planId;
    private Long mandatorId;
}
