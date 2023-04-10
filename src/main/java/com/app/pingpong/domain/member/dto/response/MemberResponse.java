package com.app.pingpong.domain.member.dto.response;

import com.app.pingpong.domain.member.entity.Member;
import lombok.*;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class MemberResponse implements Serializable {
    private Long memberId;
    private String nickname;
    private String profileImage;

    public static MemberResponse of(Member member) {
        return new MemberResponse(member.getId(), member.getNickname(), member.getProfileImage());
    }

    public static List<MemberResponse> of(List<Member> members) {
        return members.stream()
                .map(MemberResponse::of)
                .collect(Collectors.toList());
    }
}
