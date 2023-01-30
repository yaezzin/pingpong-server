package com.app.pingpong.domain.member.service;

import com.app.pingpong.domain.member.dto.request.SignUpRequest;
import com.app.pingpong.domain.member.dto.response.MemberResponse;
import com.app.pingpong.domain.member.entity.Member;
import com.app.pingpong.domain.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberResponse signup(SignUpRequest request) {
        Member member = request.toEntity(passwordEncoder);
        return MemberResponse.of(memberRepository.save(member));
    }

}
