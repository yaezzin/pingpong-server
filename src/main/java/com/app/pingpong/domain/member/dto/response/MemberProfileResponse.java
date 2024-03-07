package com.app.pingpong.domain.member.dto.response;

import com.app.pingpong.domain.member.entity.Member;
import com.app.pingpong.global.common.status.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberProfileResponse {
    private Long memberId;
    private String nickname;
    private String profileImage;
    private int friendCount;
    private Status friendStatus;

    public static MemberProfileResponse of(Member member, int friendCount, Status friendStatus) {
        return new MemberProfileResponse(member.getId(), member.getNickname(), member.getProfileImage(), friendCount, friendStatus);
    }
}