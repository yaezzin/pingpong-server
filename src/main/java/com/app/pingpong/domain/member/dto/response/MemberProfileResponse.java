package com.app.pingpong.domain.member.dto.response;

import com.app.pingpong.domain.member.entity.Member;
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
    private boolean friendStatus;

    public static MemberProfileResponse of(Member member, int friendCount, boolean friendStatus) {
        return new MemberProfileResponse(member.getId(), member.getNickname(), member.getProfileImage(), friendCount, friendStatus);
    }
}