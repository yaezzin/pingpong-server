package com.app.pingpong.domain.social.entity;

import com.app.pingpong.domain.social.dto.response.UserInfoResponse;

public interface SocialOAuth {
    String getAccessToken(String code);
    UserInfoResponse getUserInfo(String accessToken);
}
