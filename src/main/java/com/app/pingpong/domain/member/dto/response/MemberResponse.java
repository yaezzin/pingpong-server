package com.app.pingpong.domain.member.dto.response;

import com.app.pingpong.domain.member.entity.Member;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class MemberResponse implements Serializable {
    @JsonProperty("memberId")
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
