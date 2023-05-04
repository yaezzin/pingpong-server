package com.app.pingpong.domain.member.repository;

import com.app.pingpong.domain.member.entity.Member;
import com.app.pingpong.global.common.status.Authority;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static com.app.pingpong.factory.MemberFactory.createMember;
import static com.app.pingpong.global.common.status.Status.ACTIVE;
import static com.app.pingpong.global.common.status.Status.DELETE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
public class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Test
    public void findByEmail() {
        // given
        Member member = createMember();
        memberRepository.save(member);

        // when
        Optional<Member> findMember = memberRepository.findByEmail(member.getEmail());

        // then
        assertTrue(findMember.isPresent());
        assertEquals(member.getEmail(), findMember.get().getEmail());
    }

    @Test
    public void findByIdAndStatus() {
        // given
        Member member = createMember();
        memberRepository.save(member);

        // when
        Optional<Member> findMember = memberRepository.findByIdAndStatus(member.getId(), ACTIVE);

        // then
        assertTrue(findMember.isPresent());
        assertEquals(member.getId(), findMember.get().getId());
    }

    @Test
    public void findByStatusAndNicknameContains() {
        // given
        Member active = new Member("123", "email@email.com", "test1", "profileImage", ACTIVE, Authority.ROLE_USER);
        memberRepository.save(active);

        Member inactive = new Member("456", "test@email.com", "test2", "profileImage", DELETE, Authority.ROLE_USER);
        memberRepository.save(inactive);

        // when
        Optional<List<Member>> members = memberRepository.findByStatusAndNicknameContains(ACTIVE, "test");

        // then
        assertTrue(members.isPresent());
        assertEquals(1, members.get().size());
        assertEquals(active.getId(), members.get().get(0).getId());
    }

    @Test
    public void existsByEmail() {
        // given, when
        Member member = memberRepository.save(createMember());

        // then
        assertThat(memberRepository.existsByEmail(member.getEmail())).isTrue();
    }

    @Test
    public void existsMemberByNickname() {
        // given, when
        Member member = memberRepository.save(createMember());

        // then
        assertThat(memberRepository.existsMemberByNickname(member.getNickname())).isTrue();
    }

    @Test
    public void existsMemberByNicknameAndStatus() {
        // given, when
        Member member = memberRepository.save(createMember());

        // then
        memberRepository.existsMemberByNicknameAndStatus("nickname");
    }
}
