package com.app.pingpong.domain.member.repository;

import com.app.pingpong.domain.member.entity.Authority;
import com.app.pingpong.domain.member.entity.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
public class MemberRepositoryTest {

    @Autowired MemberRepository memberRepository;

    @BeforeEach
    public void init() {
        memberRepository.deleteAll();
    }

    @Test
    public void 회원가입() {
        Member member = createMember();
        memberRepository.save(member);
        Member findMember = memberRepository.findById(member.getId()).orElseThrow();
        assertThat(findMember.getId()).isEqualTo(member.getId());
    }

    @Test
    public void 이메일로_회원_찾기() {
        Member member = createMember();
        memberRepository.save(member);
        Member findMember = memberRepository.findByEmail(member.getEmail()).orElseThrow();
        assertThat(findMember.getEmail()).isEqualTo(findMember.getEmail());
    }

    private Member createMember() {
        return new Member("123", "email", "nickname", "profileImage", Authority.ROLE_USER);
    }
}
