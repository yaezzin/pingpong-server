package com.app.pingpong.domain.member.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MemberAchieveRequest {
    private LocalDate startDate;
    private LocalDate endDate;
}
