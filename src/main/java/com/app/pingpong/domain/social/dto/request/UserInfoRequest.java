package com.app.pingpong.domain.social.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserInfoRequest {
    private String socialType;
    private String code;
}
