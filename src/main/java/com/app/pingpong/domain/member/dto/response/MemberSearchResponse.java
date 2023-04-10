package com.app.pingpong.domain.member.dto.response;

import com.app.pingpong.domain.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberSearchResponse {
    private Long memberId;
    private String nickname;
    private String profileImage;
    private boolean isFriend; // 친구 여부


    public static MemberSearchResponse of(Member member, boolean isFriend) {
        return MemberSearchResponse.builder()
                .memberId(member.getId())
                .nickname(member.getNickname())
                .profileImage(member.getProfileImage())
                .isFriend(isFriend)
                .build();
    }
}
