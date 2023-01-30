package com.app.pingpong.domain.member.dto.response;

import com.app.pingpong.domain.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class MemberResponse {
    private Long userId;
    private String nickname;
    private String profileImage;

    public static MemberResponse of(Member member) {
        return new MemberResponse(member.getId(), member.getNickname(), member.getProfileImage());
    }
}
