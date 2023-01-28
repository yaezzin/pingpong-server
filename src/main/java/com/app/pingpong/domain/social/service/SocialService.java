package com.app.pingpong.domain.social.service;

import com.app.pingpong.domain.social.dto.request.UserInfoRequest;
import com.app.pingpong.domain.social.dto.response.UserInfoResponse;
import com.app.pingpong.domain.social.entity.GoogleOAuth;
import com.app.pingpong.domain.social.entity.KakaoOAuth;
import com.app.pingpong.global.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.app.pingpong.global.exception.StatusCode.INVALID_SOCIAL_TYPE;

@RequiredArgsConstructor
@Service
public class SocialService {

    private final KakaoOAuth kakao;
    private final GoogleOAuth google;

    public UserInfoResponse getUserInfo(UserInfoRequest request) {
        switch (request.getSocialType()) {
            case "KAKAO" : {
                String accessToken = kakao.getAccessToken(request.getCode());
                UserInfoResponse userInfo = kakao.getUserInfo(accessToken);
                return userInfo;
            }
            case "GOOGLE" : {
                String accessToken = google.getAccessToken(request.getCode());
                UserInfoResponse userInfo = google.getUserInfo(accessToken);
                return userInfo;
            }
            default: {
                throw new BaseException(INVALID_SOCIAL_TYPE);
            }
        }
    }



}
