package com.app.pingpong.domain.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberSearchResponse {
    private Long userId;
    private String nickname;
    private String profileImage;
    private boolean isFriend; // 친구 여부

    /*
    public static List<MemberSearchResponse> of(List<Member> members) {
        return members.stream().map(member -> MemberSearchResponse.builder()
                .userId(member.getId())
                .nickname(member.getNickname())
                .profileImage(member.getProfileImage())
                .isFriend()
                .build()).collect(Collectors.toList());
    } */
}
