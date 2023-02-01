package com.app.pingpong.domain.member.dto.response;

import com.app.pingpong.domain.friend.entity.Friend;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;


@Getter
@Builder
public class MemberFriendResponse {
    private Long id;
    private String nickname;
    private String profileImage;

    public static List<MemberFriendResponse> of(List<Friend> friends) {
        return friends.stream().map(friend -> MemberFriendResponse.builder()
                .id(friend.getId())
                .nickname(friend.getApplicant().getNickname())
                .profileImage(friend.getApplicant().getProfileImage())
                .build()).collect(Collectors.toList());
    }
}
