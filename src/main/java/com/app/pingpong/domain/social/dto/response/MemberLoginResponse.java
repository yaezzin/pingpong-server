package com.app.pingpong.domain.social.dto.response;

import com.app.pingpong.domain.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberLoginResponse {
    private Long memberId;
    private String accessToken;
    private String refreshToken;

    public static MemberLoginResponse of(Member member, TokenResponse token) {
        return new MemberLoginResponse(member.getId(), token.getAccessToken(), token.getRefreshToken());
    }
}
