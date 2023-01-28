package com.app.pingpong.domain.social.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserInfoResponse {
    private String socialIdx;
    private String email;
}
