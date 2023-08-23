package com.app.pingpong.domain.social.service;

import com.app.pingpong.domain.member.entity.Member;
import com.app.pingpong.domain.member.repository.MemberRepository;
import com.app.pingpong.domain.social.dto.request.MemberInfoRequest;
import com.app.pingpong.domain.social.dto.request.MemberLoginRequest;
import com.app.pingpong.domain.social.dto.request.MemberLogoutRequest;
import com.app.pingpong.domain.social.dto.request.TokenRequest;
import com.app.pingpong.domain.social.dto.response.MemberInfoResponse;
import com.app.pingpong.domain.social.dto.response.MemberLoginResponse;
import com.app.pingpong.domain.social.dto.response.TokenResponse;
import com.app.pingpong.domain.social.entity.GoogleOAuth;
import com.app.pingpong.domain.social.entity.KakaoOAuth;
import com.app.pingpong.global.common.exception.BaseException;
import com.app.pingpong.global.common.exception.StatusCode;
import com.app.pingpong.global.security.JwtTokenProvider;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.app.pingpong.global.common.exception.StatusCode.*;
import static com.app.pingpong.global.common.status.Status.ACTIVE;

@RequiredArgsConstructor
@Service
public class SocialService {

    private final KakaoOAuth kakao;
    private final GoogleOAuth google;
    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final RedisTemplate<String, String> redisTemplate;

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
        Member member = memberRepository.findByEmailAndStatus(request.getEmail(), ACTIVE).orElseThrow(() -> new BaseException(EMAIL_NOT_FOUND));

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(request.getEmail(), request.getSocialId());
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        TokenResponse tokenResponse = jwtTokenProvider.createToken(authentication);

        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(member.getEmail(), tokenResponse.getRefreshToken());

        return MemberLoginResponse.of(member, tokenResponse);
    }

    @Transactional
    public StatusCode logout(MemberLogoutRequest request) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        //valueOperations.getAndDelete(request.getEmail());
        //String key = valueOperations.get(request.getEmail());
        boolean deleted = valueOperations.getOperations().delete(request.getEmail());
        if (!deleted) {
            throw new BaseException(INVALID_LOGOUT_EMAIL);
        }
        return SUCCESS_LOGOUT;
    }

    @Transactional
    public TokenResponse reissue(TokenRequest tokenRequest) {
        // 1. 리프레시 토큰 검증
        if (!jwtTokenProvider.validateToken(tokenRequest.getRefreshToken())) {
            throw new BaseException(INVALID_REFRESH_TOKEN);
        }

        // 2. 인증정보 조회
        Authentication authentication = null;
        try {
            authentication = jwtTokenProvider.getAuthentication(tokenRequest.getAccessToken());
        } catch (ExpiredJwtException e) {
            // 액세스 토큰이 만료된 경우, 리프레시 토큰으로 새로운 액세스 토큰 발급
            authentication = jwtTokenProvider.getAuthenticationFromRefreshToken(tokenRequest.getRefreshToken());
        }

        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        String refreshToken = valueOperations.get(authentication.getName());

        // 3. 리프레시 토큰이 일치하는지 검증
        if (!refreshToken.equals(tokenRequest.getRefreshToken())) {
            throw new BaseException(MEMBER_NOT_FOUND);
        }

        TokenResponse token = jwtTokenProvider.createToken(authentication);
        valueOperations.set(authentication.getName(), token.getRefreshToken());

        return token;
    }
}
