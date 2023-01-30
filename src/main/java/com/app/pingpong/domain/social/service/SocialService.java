package com.app.pingpong.domain.social.service;

import com.app.pingpong.domain.member.entity.Member;
import com.app.pingpong.domain.member.repository.MemberRepository;
import com.app.pingpong.domain.social.dto.request.MemberInfoRequest;
import com.app.pingpong.domain.social.dto.request.MemberLoginRequest;
import com.app.pingpong.domain.social.dto.response.MemberInfoResponse;
import com.app.pingpong.domain.social.dto.response.MemberLoginResponse;
import com.app.pingpong.domain.social.entity.GoogleOAuth;
import com.app.pingpong.domain.social.entity.KakaoOAuth;
import com.app.pingpong.global.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.app.pingpong.global.exception.StatusCode.EMAIL_NOT_FOUND;
import static com.app.pingpong.global.exception.StatusCode.INVALID_SOCIAL_TYPE;

@RequiredArgsConstructor
@Service
public class SocialService {

    private final KakaoOAuth kakao;
    private final GoogleOAuth google;
    private final MemberRepository memberRepository;

    public MemberInfoResponse getUserInfo(MemberInfoRequest request) {
        switch (request.getSocialType()) {
            case "KAKAO": {
                String accessToken = kakao.getAccessToken(request.getCode());
                MemberInfoResponse userInfo = kakao.getUserInfo(accessToken);
                return userInfo;
            }
            case "GOOGLE": {
                String accessToken = google.getAccessToken(request.getCode());
                MemberInfoResponse userInfo = google.getUserInfo(accessToken);
                return userInfo;
            }
            default: {
                throw new BaseException(INVALID_SOCIAL_TYPE);
            }
        }
    }

    public MemberLoginResponse login(MemberLoginRequest request) {
        String email = request.getEmail();
        String socialIdx = request.getSocialIdx();
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new BaseException(EMAIL_NOT_FOUND));
        //UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(email, socialIdx);
        //Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        //TokenResponse tokenResponse = jwtTokenProvider.createToken(authentication);

        //ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        //valueOperations.set(user.getEmail(), tokenResponse.getRefreshToken());
        //RefreshToken refreshToken = RefreshToken.builder()
        //        .key(authentication.getName())
        //        .value(tokenResponse.getRefreshToken())
        //        .build();
        //refreshTokenRepository.save(refreshToken);
        //return MemberLoginResponse.of(member, tokenResponse);
        return null;
    }
}
