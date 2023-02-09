package com.app.pingpong.domain.team.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Builder
@Getter
@AllArgsConstructor
public class TeamAchieveResponse {
    private LocalDate date;
    private double achievement;
}
