package com.app.pingpong.domain.team.dto.response;

import com.app.pingpong.global.common.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class TeamMemberResponse {
    private Long memberId;
    private String nickname;
    private String profileImage;
    private Long hostId;
    private boolean isFriend;
    private Status status;
}
