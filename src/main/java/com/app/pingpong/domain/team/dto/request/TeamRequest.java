package com.app.pingpong.domain.team.dto.request;

import com.app.pingpong.domain.team.entity.Team;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TeamRequest {
    private String name;
    private List<Long> memberId;

    public Team toEntity() {
        return Team.builder()
                .name(name)
                .build();
    }
}
