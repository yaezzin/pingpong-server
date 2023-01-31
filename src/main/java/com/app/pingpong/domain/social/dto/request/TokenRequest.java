package com.app.pingpong.domain.social.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TokenRequest {
    private String accessToken;
    private String refreshToken;
}
