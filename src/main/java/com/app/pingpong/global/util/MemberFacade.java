package com.app.pingpong.global.util;

import com.app.pingpong.domain.member.entity.Member;
import com.app.pingpong.domain.member.repository.MemberRepository;
import com.app.pingpong.global.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import static com.app.pingpong.global.exception.StatusCode.MEMBER_NOT_FOUND;

@RequiredArgsConstructor
@Component
public class MemberFacade {

    private final MemberRepository memberRepository;

    public Member getCurrentMember() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        if(email.equals("anonymousUser"))
            throw new BaseException(MEMBER_NOT_FOUND);
        return memberRepository.findByEmail(email).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));
    }
}
