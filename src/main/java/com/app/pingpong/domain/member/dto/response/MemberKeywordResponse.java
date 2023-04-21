package com.app.pingpong.domain.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberKeywordResponse {
    private String keyword;

    public static MemberKeywordResponse of(String keyword) {
        return new MemberKeywordResponse(keyword);
    }
}
