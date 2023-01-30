package com.app.pingpong.domain.member.service;

import com.app.pingpong.domain.member.dto.request.SignUpRequest;
import com.app.pingpong.domain.member.dto.response.MemberResponse;
import com.app.pingpong.domain.member.entity.Authority;
import com.app.pingpong.domain.member.entity.Member;
import com.app.pingpong.domain.member.repository.MemberRepository;

import com.app.pingpong.global.common.BaseResponse;
import com.app.pingpong.global.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.app.pingpong.global.exception.StatusCode.*;
import static com.app.pingpong.global.util.RegexUtil.isRegexNickname;

@RequiredArgsConstructor
@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public MemberResponse signup(SignUpRequest request) {
        Member member = request.toEntity(passwordEncoder);
        return MemberResponse.of(memberRepository.save(member));
    }

    @Transactional
    public BaseResponse<String> validateNickname(String nickname){
        if (!isRegexNickname(nickname)) {
            throw new BaseException(INVALID_NICKNAME);
        }
        if (memberRepository.existsUserByNickname(nickname)) {
            throw new BaseException(USER_NICKNAME_ALREADY_EXISTS);
        }
        return new BaseResponse(SUCCESS_VALIDATE_NICKNAME);
    }

}
