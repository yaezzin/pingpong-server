package com.app.pingpong.domain.team.dto.response;

import com.app.pingpong.domain.member.entity.Member;
import com.app.pingpong.domain.member.entity.MemberTeam;
import com.app.pingpong.domain.team.entity.Team;
import com.app.pingpong.global.common.status.Status;
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
    private Status friendship;
    private Status status;

    public static TeamMemberResponse of(Member findMember, Team team, Status isFriend, MemberTeam isStatus) {
        return TeamMemberResponse.builder()
                .memberId(findMember.getId())
                .nickname(findMember.getNickname())
                .profileImage(findMember.getProfileImage())
                .hostId(team.getHost().getId())
                .friendship(isFriend)
                .status(isStatus.getStatus())
                .build();
    }
}
