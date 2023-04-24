package com.app.pingpong.domain.member.repository;

import com.app.pingpong.global.common.status.Authority;
import com.app.pingpong.domain.member.entity.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static com.app.pingpong.global.common.status.Status.ACTIVE;
import static com.app.pingpong.global.common.status.Status.DELETE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
public class MemberRepositoryTest {

    @Autowired MemberRepository memberRepository;

    @Test
    @DisplayName("이메일로 멤버 조회")
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
    @DisplayName("ID와 상태값으로 멤버 조회")
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
    @DisplayName("상태값이 Active이고, 닉네임에 해당 문자열이 포함되는 멤버 조회")
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
    @DisplayName("이메일 중복 체크")
    public void existsByEmail() {
        // given, when
        Member member = memberRepository.save(createMember());

        // then
        assertThat(memberRepository.existsByEmail(member.getEmail())).isTrue();
    }

    @Test
    @DisplayName("닉네임 중복 체크")
    public void existsMemberByNickname() {
        // given, when
        Member member = memberRepository.save(createMember());

        // then
        assertThat(memberRepository.existsMemberByNickname(member.getNickname())).isTrue();
    }

    @Test
    @DisplayName("상태값이 active이고, 해당 닉네임으로 유저가 존재하는지 확인")
    public void existsMemberByNicknameAndStatus() {
        // given, when
        Member member = memberRepository.save(createMember());

        // then
        memberRepository.existsMemberByNicknameAndStatus("nickname");
    }

    private Member createMember() {
        return new Member("123", "email", "nickname", "profileImage", ACTIVE, Authority.ROLE_USER);
    }
}
