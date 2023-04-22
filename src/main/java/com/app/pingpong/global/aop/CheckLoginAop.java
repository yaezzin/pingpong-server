package com.app.pingpong.global.aop;

import com.app.pingpong.domain.member.repository.MemberRepository;
import com.app.pingpong.global.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import static com.app.pingpong.global.exception.StatusCode.MEMBER_NOT_FOUND;

@Aspect
@Component
@RequiredArgsConstructor
public class CheckLoginAop {

    private final MemberRepository memberRepository;

    @Before(value = "@annotation(CheckLoginStatus) && @annotation(checkLoginStatus)")
    public void checkStatus(CheckLoginStatus checkLoginStatus) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        if(email.equals("anonymousUser"))
            throw new BaseException(MEMBER_NOT_FOUND);
        memberRepository.findByEmail(email).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));
    }
}
