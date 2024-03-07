package com.app.pingpong.domain.member.dto.response;

import com.app.pingpong.domain.member.entity.Member;
import com.app.pingpong.global.common.status.Status;
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
    private Status friendStatus;


    public static MemberSearchResponse of(Member member, Status friendStatus) {
        return MemberSearchResponse.builder()
                .memberId(member.getId())
                .nickname(member.getNickname())
                .profileImage(member.getProfileImage())
                .friendStatus(friendStatus)
                .build();
    }
}
