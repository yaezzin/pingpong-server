package com.app.pingpong.domain.social.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberLoginRequest {
    private String socialIdx;
    private String email;
}
