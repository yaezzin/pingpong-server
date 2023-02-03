package com.app.pingpong.domain.team.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class TeamMemberResponse {
    private Long userId;
    private String nickname;
    private String profileImage;
    private Long hostId;
    private boolean isFriend;
}
