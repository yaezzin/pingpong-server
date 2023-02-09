package com.app.pingpong.domain.team.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TeamAchieveRequest {
    private LocalDate startDate;
    private LocalDate endDate;
}
