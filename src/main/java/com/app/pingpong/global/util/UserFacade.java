package com.app.pingpong.global.util;

import com.app.pingpong.domain.member.entity.Member;
import com.app.pingpong.domain.member.repository.MemberRepository;
import com.app.pingpong.global.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import static com.app.pingpong.global.exception.StatusCode.USER_NOT_FOUND;

@RequiredArgsConstructor
@Component
public class UserFacade {

    private final MemberRepository userRepository;

    public Member getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        if(email.equals("anonymousUser"))
            throw new BaseException(USER_NOT_FOUND);
        return userRepository.findByEmail(email).orElseThrow(() -> new BaseException(USER_NOT_FOUND));
    }
}
