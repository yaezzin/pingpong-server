package com.app.pingpong.domain.friend.repository;

import com.app.pingpong.domain.friend.entity.Friend;
import com.app.pingpong.domain.member.entity.Member;
import com.app.pingpong.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static com.app.pingpong.factory.MemberFactory.createMember;
import static com.app.pingpong.global.common.status.Status.ACTIVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.DateUtil.now;

@DataJpaTest
public class FriendRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    FriendRepository friendRepository;

    @Test
    @DisplayName("memberId를 통한 친구 전체 조회")
    public void findAllFriendsByMemberId() {
        // given
        Member loginMember = memberRepository.save(createMember());
        Member opponentMember = memberRepository.save(createMember());
        Friend friend = new Friend(loginMember.getId(), opponentMember.getId(), ACTIVE, now());
        friendRepository.save(friend);

        // when
        List<Member> list = friendRepository.findAllFriendsByMemberId(loginMember.getId());

        // then
        assertThat(list.size()).isEqualTo(2);
    }
}
