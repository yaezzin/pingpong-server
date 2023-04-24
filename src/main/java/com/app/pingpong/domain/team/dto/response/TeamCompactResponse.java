package com.app.pingpong.domain.team.dto.response;

import com.app.pingpong.global.common.status.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class TeamCompactResponse {
    private Long memberId;
    private Status status;
}
