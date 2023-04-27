package com.app.pingpong.domain.member.repository;

import com.app.pingpong.domain.member.entity.Member;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static com.app.pingpong.global.common.status.Authority.ROLE_USER;
import static com.app.pingpong.global.common.status.Status.ACTIVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class MemberSearchRepositoryTest {

    @Test
    @DisplayName("no offset 방식으로 해당 닉네임을 가진 유저 조회 테스트")
    public void findByNicknameContainsWithNoOffset() {
        // given
        MemberSearchRepository memberSearchRepository = mock(MemberSearchRepository.class);

        List<Member> memberList = new ArrayList<>();
        for (int i = 0; i <= 100; i++) {
            Member member = new Member("socialId" + i, "email" + i, "nickname" + i, "profileImage", ACTIVE, ROLE_USER);
            memberList.add(member);
        }
        given(memberSearchRepository.findByNicknameContainsWithNoOffset(eq(ACTIVE), eq("nickname"), eq(10L), eq(10)))
                .willReturn(memberList.subList(10, 20));

        // when
        List<Member> members = memberSearchRepository.findByNicknameContainsWithNoOffset(ACTIVE, "nickname", 10L, 10);

        //then
        assertThat(members).hasSize(10);
        assertThat(members.get(0).getId()).isEqualTo(memberList.get(10).getId());
    }


}

