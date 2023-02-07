package com.app.pingpong.domain.member.dto.response;

import com.app.pingpong.domain.member.entity.Member;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

@Getter
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
//@JsonInclude(JsonInclude.Include.NON_NULL)
public class MemberResponse implements Serializable {
    private Long userId;
    private String nickname;
    private String profileImage;

    public static MemberResponse of(Member member) {
        return new MemberResponse(member.getId(), member.getNickname(), member.getProfileImage());
    }
}
