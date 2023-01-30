package com.app.pingpong.domain.member.repository;

import com.app.pingpong.domain.member.entity.Authority;
import com.app.pingpong.domain.member.entity.Member;
import com.app.pingpong.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
public class MemberTest {

    @Autowired MemberRepository memberRepository;

    @Test
    public void 회원가입() {
        //given
        Member member = new Member("123", "email", "nickname", "profileImage", Authority.ROLE_USER);

        //when
        memberRepository.save(member);

        //then
        Member findMember = memberRepository.findById(member.getId()).orElseThrow();
        assertThat(findMember.getId()).isEqualTo(member.getId());
    }
}
