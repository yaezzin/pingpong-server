package com.app.pingpong.domain.social.entity;

import com.app.pingpong.domain.social.dto.response.UserInfoResponse;

public class GoogleOAuth implements SocialOAuth {

    @Override
    public String getAccessToken(String code) {
        return "~~~";
    }

    @Override
    public UserInfoResponse getUserInfo(String accessToken) {
        return null;
    }

}
