package com.app.pingpong.domain.member.service;

import com.app.pingpong.domain.member.entity.Authority;
import com.app.pingpong.domain.member.entity.Member;
import com.app.pingpong.domain.member.repository.MemberRepository;
import com.app.pingpong.domain.social.dto.request.MemberLoginRequest;
import com.app.pingpong.domain.social.dto.response.MemberLoginResponse;
import com.app.pingpong.domain.social.service.SocialService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
public class MemberServiceTest {

    @Autowired MemberRepository memberRepository;
    @Autowired SocialService socialService;

    @BeforeEach
    public void init() {
        memberRepository.deleteAll();
    }

    private Member createMember() {
        return new Member("123", "email", "nickname", "profileImage", Authority.ROLE_USER);
    }
}
